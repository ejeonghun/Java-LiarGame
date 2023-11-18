import javax.sound.sampled.*;

public class AudioCapturePlayback {

    private TargetDataLine line;
    private SourceDataLine sline;

    public void captureAudio() {
        try {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            DataLine.Info sinfo = new DataLine.Info(SourceDataLine.class, format);
            sline = (SourceDataLine) AudioSystem.getLine(sinfo);
            sline.open(format);
            sline.start();

            Runnable runner = new Runnable() {
                int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
                byte buffer[] = new byte[bufferSize];

                public void run() {
                    try {
                        int count;
                        while ((count = line.read(buffer, 0, buffer.length)) != -1) {
                            if (count > 0) {
                                sline.write(buffer, 0, count);
                            }
                        }
                    } finally {
                        sline.drain();
                        sline.close();
                    }
                }
            };
            Thread captureThread = new Thread(runner);
            captureThread.start();
        } catch (LineUnavailableException e) {
            System.err.println("Line Unavailable: " + e);
            System.exit(-2);
        }
    }
}