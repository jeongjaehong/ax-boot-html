package util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class FakeHostnameVerifier implements HostnameVerifier {
	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}
}
