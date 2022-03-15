package util;

import java.util.ArrayList;
import java.util.List;

public class LogRecords extends ArrayList<MCLogLine> {
	private static final long serialVersionUID = 1L;
	private String filter;
	private List<MCLogLine> logLines;

	public LogRecords(List<MCLogLine> relevantLogLines) {
		this.logLines = relevantLogLines;
	}

	/**
	 * Starts parsing a recorded activity like a dungeon run that starts at a
	 * specific log line and returns the line after the record ends and the record
	 * was added to a list
	 * 
	 * @param recordStartLine - start line
	 * @param logLines        - the list with all the MinecraftLogLine objects
	 * @return - the line where the parsing should continue
	 */
	public void add(int recordStartLine) {
		int lineIndex = recordStartLine;
		MCLogLine line = logLines.get(lineIndex);

		String[] filters = splitRegexByMainOr(filter);
		int matchIndex = line.matches(filters);
		if (matchIndex >= 0) {

			add(line);
			// line = logLines.get(lineIndex = (lineIndex < logLines.size() - 1) ?
			// (lineIndex + 1) : lineIndex);
		}
		return;
	}

	private String[] splitRegexByMainOr(String regex) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int deg = 0;
		char c = ' ';
		for (int i = 0; i < regex.length(); i++) {
			c = regex.charAt(i);
			if (c == '\\')
				continue;
			else if (c == '{' || c == '[' || c == '(' || c == '<')
				deg = 1;
			else if (c == '}' || c == ']' || c == ')' || c == '>')
				deg = 0;
		}
		int start = 0;
		return regex.split("\\|"); // TODO implement
	}

	public String getLoglineFilterRegex() {
		// TODO replace special multiline operators
		// "(regex)|(regex)|(regex)->(regex)->(regex)" by "regex|regex"
		return filter;
	}

	public void setLoglineFilterRegex(String filter) {
		this.filter = filter;
	}
}
