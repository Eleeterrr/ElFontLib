package eleeter.elfontlib.emoji.animated;

import eleeter.elfontlib.emoji.build.EmojiBuildException;

import java.io.File;
import java.util.List;


public interface FrameDecoder
{
    List<DecodedFrame> decode(File sourceFile) throws EmojiBuildException;
}
