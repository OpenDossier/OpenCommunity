package ch.opencommunity.base;

import java.math.BigInteger;
import java.util.Random;

import ch.opencommunity.base.Document.DocumentFormat;

public abstract class BasicDocument extends BasicOCObject {
	private String tempID;
	
	public String getFullFilename(boolean filesystem) {
		return getFullFilename(filesystem, null);
	}
	public String getFullFilename(boolean filesystem, DocumentFormat documentFormat) {
		return getDocPath(filesystem, documentFormat) + getName() + getDocPostfix(documentFormat); 
	}
	
	protected String getTempName() {
		return getTempName(getTempPrefix());
	}
	protected String getTempName(String prefix) {
		if (tempID == null) {
			tempID = new BigInteger(50, new Random()).toString();
		}
		return "od_" + prefix + "_" + tempID;
	}

	protected abstract String getTempPrefix();
	protected abstract String getDocPath(boolean filesystem); 
	protected abstract String getDocPath(boolean filesystem, DocumentFormat documentFormat);
	protected abstract String getDocPostfix();
	protected abstract String getDocPostfix(DocumentFormat documentFormat);
	
	public abstract boolean isExternal();
	public abstract void setIsExternal(boolean isExternal);
}
