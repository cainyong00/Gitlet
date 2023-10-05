package gitlet;
import java.io.File;

import java.io.Serializable;

public class Blob implements Serializable {

    /** source file. */
    private final File _source;
    /** content of file. */
    private final byte[] _content;
    /** sha1 of file. */
    private final String _sha1;
    /** filename. */
    private final String _fileName;
    /** string content of file. */
    private final String _stringContent;

    public Blob(File path) {
        _source = path;
        _content = Utils.readContents(path);
        _sha1 = Utils.sha1(_content, path.getName());
        _fileName = path.getName();
        _stringContent = Utils.readContentsAsString(path);
    }

    public String getSha1() {
        return _sha1;
    }
    public static byte[] getContent(Blob blob) {
        return blob._content;
    }
    public File getSource() {
        return _source;
    }
    public String getFileName() {
        return _fileName;
    }
    public String getStringContent() {
        return _stringContent;
    }

}
