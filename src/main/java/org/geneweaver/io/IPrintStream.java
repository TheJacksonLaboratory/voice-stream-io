package org.geneweaver.io;

import java.io.PrintStream;

public interface IPrintStream {

	/**
	 * Print a line to the stream
	 * @param s
	 */
	void println(String s);
	
	/**
	 * Get a temporary print stream.
	 * This can only be used for one operation e.g. printStackTrace(...)
	 * because of maximum line management which means the stream can be closed.
	 * @return the current underlying stream (which may change so do not store this stream!).
	 */
	PrintStream getPrintStream();

	public static IPrintStream of(PrintStream out) {
		return new IPrintStream() {
			@Override
            public void println(String s) {
                out.println(s);
            }

			@Override
			public PrintStream getPrintStream() {
				return out;
			}
		};
	}
}
