package builder.framework.asset;

import java.io.StringBufferInputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * 让jdom不进行dtd验证，因为如果验证网络不通则会报错
 * @author hhcao
 * @date 2015年8月5日
 * @version 1.0
 */
public class NoOpEntityResolver implements EntityResolver {
	  public InputSource resolveEntity(String publicId, String systemId) {
	             return new InputSource(new StringBufferInputStream(""));
	  }
}

