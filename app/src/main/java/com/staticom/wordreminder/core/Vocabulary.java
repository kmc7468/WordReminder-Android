package com.staticom.wordreminder.core;

import com.staticom.wordreminder.utility.BinaryStream;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vocabulary implements Serializable {

    private enum ContainerId {
        HOMONYM_CONTAINER,
        EXAMPLE_CONTAINER,
        TAG_CONTAINER,
        RELATION_CONTAINER,

        UNKNOWN,
    }

    private interface ContainerReader {
        void read(BinaryStream containerStream) throws IOException;
    }

    private interface ContainerWriter {
        void write(BinaryStream containerStream) throws IOException;
    }

    private final List<Word> words = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();

    private boolean hasUnreadableContainers;

    public List<Word> getWords() {
        return Collections.unmodifiableList(words);
    }

    public boolean containsWord(String word) {
        return indexOfWord(word) != -1;
    }

    public boolean containsWord(Word word) {
        return words.contains(word);
    }

    public int indexOfWord(String word) {
        for (int i = 0; i < words.size(); ++i) {
            if (words.get(i).getWord().equals(word)) return i;
        }

        return -1;
    }

    public Word findWord(String word) {
        return words.get(indexOfWord(word));
    }

    public Word getWord(int index) {
        return words.get(index);
    }

    public void addWord(Word word) {
        word.setVocabulary(this);

        words.add(word);
    }

    public void addWordRef(Word word) {
        words.add(word);
    }

    public void removeWord(Word word) {
        if (word.getVocabulary() == this) {
            word.setVocabulary(null);
        }

        words.remove(word);
    }

    public int getMeaningCount() {
        int result = 0;

        for (final Word word : words) {
            result += word.getMeanings().size();
        }

        return result;
    }

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public boolean containsTag(String tag) {
        for (final Tag t : tags) {
            if (t.getTag().equals(tag)) return true;
        }

        return false;
    }

    public Tag getTag(int index) {
        return tags.get(index);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public boolean hasUnreadableContainers() {
        return hasUnreadableContainers;
    }

    private static void writeContainer(BinaryStream fileStream,
                                       ContainerId id, boolean require, ContainerWriter containerWriter) throws IOException {
        if (!require) return;

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final BinaryStream containerStream = new BinaryStream(stream);

        containerWriter.write(containerStream);

        final byte[] bytes = stream.toByteArray();

        fileStream.writeInt(id.ordinal());
        fileStream.writeInt(bytes.length);
        fileStream.write(bytes);
    }

    public void writeToFileStream(FileOutputStream stream) throws IOException {
        final BinaryStream fileStream = new BinaryStream(stream);

        boolean needHomonymContainer = false,
                needExampleContainer = false,
                needRelationContainer = false;
        int containerCount = 0;

        final boolean needTagContainer = !getTags().isEmpty();
        if (needTagContainer) {
            ++containerCount;
        }

        fileStream.writeInt(words.size());

        for (final Word word : words) {
            final Meaning mergedMeaning = word.mergeMeanings();

            fileStream.writeString(word.getWord());
            fileStream.writeString(mergedMeaning.getPronunciation());
            fileStream.writeString(mergedMeaning.getMeaning());

            if (word.getMeanings().size() > 1 && !needHomonymContainer) {
                needHomonymContainer = true;
                ++containerCount;
            }

            if (word.hasExample() && !needExampleContainer) {
                needExampleContainer = true;
                ++containerCount;
            }

            if (word.hasRelations() && !needRelationContainer) {
                needRelationContainer = true;
                ++containerCount;
            }
        }

        if (containerCount == 0) return;

        fileStream.writeInt(containerCount);

        writeContainer(fileStream, ContainerId.HOMONYM_CONTAINER, needHomonymContainer, containerStream -> {
            for (final Word word : words) {
                containerStream.writeInt(word.getMeanings().size());

                for (final Meaning meaning : word.getMeanings()) {
                    containerStream.writeString(meaning.getPronunciation());
                    containerStream.writeString(meaning.getMeaning());
                }
            }
        });

        writeContainer(fileStream, ContainerId.EXAMPLE_CONTAINER, needExampleContainer, containerStream -> {
            for (final Word word : words) {
                for (final Meaning meaning : word.getMeanings()) {
                    containerStream.writeString(meaning.getExample());
                }
            }
        });

        writeContainer(fileStream, ContainerId.TAG_CONTAINER, needTagContainer, containerStream -> {
            containerStream.writeInt(tags.size());

            for (final Tag tag : tags) {
                containerStream.writeString(tag.getTag());
            }

            for (final Word word : words) {
                for (final Meaning meaning : word.getMeanings()) {
                    containerStream.writeInt(meaning.getTags().size());

                    for (final Tag tag : meaning.getTags()) {
                        containerStream.writeInt(tags.indexOf(tag));
                    }
                }
            }
        });

        writeContainer(fileStream, ContainerId.RELATION_CONTAINER, needRelationContainer, containerStream -> {
            for (final Word word : words) {
                containerStream.writeInt(word.getRelations().size());

                for (final Relation relation : word.getRelations()) {
                    containerStream.writeInt(words.indexOf(relation.getWord()));
                    containerStream.writeString(relation.getRelation());
                }
            }
        });
    }

    private static ContainerId castToContainerId(int id) {
        return ContainerId.values()[Math.min(id, ContainerId.UNKNOWN.ordinal())];
    }

    private static boolean readContainer(BinaryStream fileStream, boolean read,
                                         ContainerId id, ContainerId realId, ContainerReader containerReader) throws IOException {
        if (id == realId) {
            containerReader.read(fileStream);

            return true;
        } else return read;
    }

    public static Vocabulary readFromFileStream(FileInputStream stream) throws IOException {
        final BinaryStream fileStream = new BinaryStream(stream);
        final Vocabulary vocabulary = new Vocabulary();

        final int wordCount = fileStream.readInt();
        for (int i = 0; i < wordCount; ++i) {
            final Word word = new Word(fileStream.readString());
            final String pronunciation = fileStream.readString();
            final String meaning = fileStream.readString();

            word.addMeaning(new Meaning(meaning, pronunciation));

            vocabulary.addWord(word);
        }

        final int containerCount;

        try {
            containerCount = fileStream.readInt();
        } catch (final Exception e) {
            return vocabulary;
        }

        for (int i = 0; i < containerCount; ++i) {
            final ContainerId id = castToContainerId(fileStream.readInt());
            final int length = fileStream.readInt();
            boolean read = false;

            read = readContainer(fileStream, read, ContainerId.HOMONYM_CONTAINER, id, containerStream -> {
                for (final Word word : vocabulary.words) {
                    word.removeMeaning(0);

                    final int meaningCount = containerStream.readInt();
                    for (int j = 0; j < meaningCount; ++j) {
                        final String pronunciation = containerStream.readString();
                        final String meaning = containerStream.readString();

                        word.addMeaning(new Meaning(meaning, pronunciation));
                    }
                }
            });

            read = readContainer(fileStream, read, ContainerId.EXAMPLE_CONTAINER, id, containerStream -> {
                for (final Word word : vocabulary.words) {
                    for (final Meaning meaning : word.getMeanings()) {
                        meaning.setExample(containerStream.readString());
                    }
                }
            });

            read = readContainer(fileStream, read, ContainerId.TAG_CONTAINER, id, containerStream -> {
                final int tagCount = containerStream.readInt();
                for (int j = 0; j < tagCount; ++j) {
                    vocabulary.tags.add(new Tag(vocabulary, containerStream.readString()));
                }

                for (final Word word : vocabulary.words) {
                    for (final Meaning meaning : word.getMeanings()) {
                        final int meaningTagCount = containerStream.readInt();
                        for (int j = 0; j < meaningTagCount; ++j) {
                            meaning.addTag(vocabulary.tags.get(containerStream.readInt()));
                        }
                    }
                }
            });

            read = readContainer(fileStream, read, ContainerId.RELATION_CONTAINER, id, containerStream -> {
                for (final Word word : vocabulary.words) {
                    final int relationCount = containerStream.readInt();
                    for (int j = 0; j < relationCount; ++j) {
                        final int wordIndex = containerStream.readInt();
                        final String relation = containerStream.readString();

                        word.addRelation(vocabulary.words.get(wordIndex), relation);
                    }
                }
            });

            if (!read) {
                fileStream.skip(length);

                vocabulary.hasUnreadableContainers = true;
            }
        }

        return vocabulary;
    }

    public Vocabulary search(String query) {
        final String queryLowerCase = query.toLowerCase();
        final Vocabulary searchResult = new Vocabulary();

        for (final Word word : words) {
            if (word.getWord().toLowerCase().contains(queryLowerCase)) {
                searchResult.addWordRef(word);

                continue;
            }

            for (final Meaning meaning : word.getMeanings()) {
                if (meaning.getMeaning().toLowerCase().contains(queryLowerCase) ||
                        meaning.getPronunciation().toLowerCase().contains(queryLowerCase) ||
                        meaning.getExample().toLowerCase().contains(queryLowerCase)) {
                    searchResult.addWordRef(word);

                    break;
                }
            }
        }

        return searchResult;
    }
}