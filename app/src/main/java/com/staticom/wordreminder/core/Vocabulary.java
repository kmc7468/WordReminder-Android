package com.staticom.wordreminder.core;

import com.staticom.wordreminder.utility.BinaryStream;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Vocabulary {

    private enum ContainerId {
        HOMONYM_CONTAINER,
        EXAMPLE_CONTAINER,
    }

    private interface ContainerReader {
        void read(BinaryStream containerStream) throws IOException;
    }

    private interface ContainerWriter {
        void write(BinaryStream containerStream) throws IOException;
    }

    private String name;
    private String path;
    private LocalDateTime time;

    private final List<Word> words = new ArrayList<>();

    public Vocabulary(String name) {
        this.name = name;
        this.path = UUID.randomUUID().toString() + ".kv";
        this.time = LocalDateTime.now();
    }

    private Vocabulary(String name, String path, LocalDateTime time) {
        this.name = name;
        this.path = path;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

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

    private static boolean readContainer(BinaryStream fileStream,
                                         ContainerId id, ContainerId realId, ContainerReader reader) throws IOException {
        if (id == realId) {
            reader.read(fileStream);

            return true;
        } else return false;
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
    }

    public static Vocabulary readFromFileStream(String name, String path, LocalDateTime time, FileInputStream stream) throws IOException {
        final BinaryStream fileStream = new BinaryStream(stream);

        final Vocabulary vocabulary = new Vocabulary(name, path, time);

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
            containerCount = stream.read();
        } catch (Exception e) {
            return vocabulary;
        }

        for (int i = 0; i < containerCount; ++i) {
            final ContainerId id = ContainerId.values()[fileStream.readInt()];
            final int length = fileStream.readInt();
            boolean read = false;

            read = readContainer(fileStream, ContainerId.HOMONYM_CONTAINER, id, containerStream -> {
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

            read = read || readContainer(fileStream, ContainerId.EXAMPLE_CONTAINER, id, containerStream -> {
                for (final Word word : vocabulary.words) {
                    for (final Meaning meaning : word.getMeanings()) {
                        meaning.setExample(containerStream.readString());
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