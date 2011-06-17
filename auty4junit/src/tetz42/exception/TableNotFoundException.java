package tetz42.exception;

public class TableNotFoundException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -6204111196978982565L;

	public TableNotFoundException(String msg) {
		super(msg);
	}

	public TableNotFoundException(String msg, Throwable t) {
		super(msg, t);
	}

}
