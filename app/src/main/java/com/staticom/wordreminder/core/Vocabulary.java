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
    }

    private interface ContainerReader {
        void read(BinaryStream containerStream) throws IOException;
    }

    private interface ContainerWriter {
        void write(BinaryStream containerStream) throws IOException;
    }

    private final List<Word> words = new ArrayList<>();
    private final List<Tag> tags = new ArrayList<>();

    public List<Word> getWords() {
        return Collections.unmodifiableList(words);
    }

    public Word getWord(int index) {
        return words.get(index);
    }

    public int indexOfWord(String word) {
        for (int i = 0; i < words.size(); ++i) {
            if (words.get(i).getWord().equals(word)) return i;
        }

        return -1;
    }

    public boolean containsWord(String word) {
        return indexOfWord(word) != -1;
    }

    public boolean containsWord(Word word) {
        return words.contains(word);
    }

    public Word findWord(String word) {
        return words.get(indexOfWord(word));
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

    public List<Tag> getTags() {
        return Collections.unmodifiableList(tags);
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

    private static boolean readContainer(BinaryStream fileStream, boolean read,
                                         ContainerId id, ContainerId realId, ContainerReader reader) throws IOException {
        if (id == realId) {
            reader.read(fileStream);

            return true;
        } else return read;
    }

    private static void writeContainer(BinaryStream fileStream,
                                       ContainerId id, boolean require, ContainerWriter writer) throws IOException {
        if (!require) return;

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final BinaryStream containerStream = new BinaryStream(stream);

        writer.write(containerStream);

        final byte[] bytes = stream.toByteArray();

        fileStream.writeInt(id.ordinal());
        fileStream.writeInt(bytes.length);
        fileStream.write(bytes);
    }

    public void writeToFileStream(FileOutputStream stream) throws IOException {
        final BinaryStream fileStream = new BinaryStream(stream);

        boolean needHomonymContainer = false,
                needExampleContainer = false;
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

        int containerCount;

        try {
            containerCount = fileStream.readInt();
        } catch (Exception e) {
            return vocabulary;
        }

        for (int i = 0; i < containerCount; ++i) {
            final ContainerId id = ContainerId.values()[fileStream.readInt()];
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
                    vocabulary.tags.add(new Tag(containerStream.readString(), vocabulary));
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

            if (!read) {
                fileStream.skip(length);
            }
        }

        return vocabulary;
    }
}