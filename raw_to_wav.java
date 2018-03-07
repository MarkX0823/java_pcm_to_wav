private void rawToWave(final ArrayList<short[]> shortDataList, final File waveFile) {
    int length = 0;

    for (short[] s : shortDataList) {
        length += s.length;
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(length * 2);
    for (short[] s : shortDataList) {
        for (short s2 : s) {
            byteBuffer.putShort(s2);
        }
    }

    byte[] rawData = byteBuffer.array();

    DataOutputStream output = null;
    try {
        output = new DataOutputStream(new FileOutputStream(waveFile));
        // WAVE header
        // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
        writeString(output, "RIFF"); // chunk id
        writeInt(output, 36 + rawData.length); // chunk size
        writeString(output, "WAVE"); // format
        writeString(output, "fmt "); // subchunk 1 id
        writeInt(output, 16); // subchunk 1 size
        writeShort(output, (short) 1); // audio format (1 = PCM)
        writeShort(output, (short) 1); // number of channels
        writeInt(output, tmpSamplingRate); // sample rate
        writeInt(output, tmpSamplingRate * 2); // byte rate
        writeShort(output, (short) 2); // block align
        writeShort(output, (short) 16); // bits per sample
        writeString(output, "data"); // subchunk 2 id
        writeInt(output, rawData.length); // subchunk 2 size
        // Audio data (conversion big endian -> little endian)
        short[] shorts = new short[rawData.length / 2];
        ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
        for (short s : shorts) {
            bytes.putShort(s);
        }
        output.write(bytes.array());
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        if (output != null) {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

private void writeInt(final DataOutputStream output, final int value) throws IOException {
    output.write(value >> 0);
    output.write(value >> 8);
    output.write(value >> 16);
    output.write(value >> 24);
}

private void writeShort(final DataOutputStream output, final short value) throws IOException {
    output.write(value >> 0);
    output.write(value >> 8);
}

private void writeString(final DataOutputStream output, final String value) throws IOException {
    for (int i = 0; i < value.length(); i++) {
        output.write(value.charAt(i));
    }
}
