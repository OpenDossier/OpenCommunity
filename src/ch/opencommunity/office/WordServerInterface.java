package ch.opencommunity.office;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.opencommunity.common.OpenCommunityUserSession;

public interface WordServerInterface {
	public boolean onWordClientAction(String action, HttpServletRequest request, HttpServletResponse response,
			OpenCommunityUserSession userSession) throws IOException;

}
