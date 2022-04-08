package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LogRecords extends ArrayList<MCLogLine> {
	private static final long serialVersionUID = 1L;
	private String filter;
	private ArrayList<ArrayList<String>> extractedFilters;
	private String lineFilter;
	private List<MCLogLine> logLines;

	public LogRecords(List<MCLogLine> relevantLogLines, String filter) {
		this.logLines = relevantLogLines;
		this.filter = filter;
		this.extractedFilters = extractFilters();
		ArrayList<String> results = new ArrayList<>();
		for (ArrayList<String> array : extractedFilters)
			results.add(String.join("|", array));
		lineFilter = String.join("|", results);
	}

	private ArrayList<ArrayList<String>> extractFilters() {
		ArrayList<ArrayList<String>> results = new ArrayList<>();
		for (String filterRegex : parseFilters(filter))
			results.add(parseFilter(filterRegex));
		return results;
	}

	private ArrayList<String> parseFilters(String regex) {
		ArrayList<Integer> indices = new ArrayList<>();
		Stack<Integer> brackets = new Stack<>();
		int level = 0;
		int bracketType = -1;
		char lastChar = ' ';
		char currentChar = ' ';
		for (int i = 0; i < regex.length(); i++) {
			currentChar = regex.charAt(i);
			if (lastChar == '\\' || "^$?*+".indexOf(currentChar) >= 0)
				;
			else if (level == 0 && "|".indexOf(currentChar) >= 0)
				indices.add(i + 1);
			else if ((bracketType = "([{".indexOf(currentChar)) >= 0) {
				brackets.push(bracketType);
				level++;
			} else if ((bracketType = ")]}".indexOf(currentChar)) >= 0) {
				if (bracketType == brackets.peek()) {
					brackets.pop();
					level--;
				}
			}
			lastChar = currentChar;
		}
		ArrayList<String> results = new ArrayList<>();
		int lastIndex = 0;
		for (int i : indices)
			results.add(regex.substring(lastIndex, (lastIndex = i) - 1));
		results.add(regex.substring(lastIndex));
		return results;
	}

	private ArrayList<String> parseFilter(String regex) {
		ArrayList<Integer> indices = new ArrayList<>();
		Stack<Integer> brackets = new Stack<>();
		int level = 0;
		int bracketType = -1;
		char lastChar = ' ';
		char currentChar = ' ';
		for (int i = 0; i < regex.length(); i++) {
			currentChar = regex.charAt(i);
			if (lastChar == '\\' || "^$?*+".indexOf(currentChar) >= 0)
				;
			else if (level == 0 && lastChar == '-' && currentChar == '>')
				indices.add(i + 1);
			else if ((bracketType = "([{".indexOf(currentChar)) >= 0) {
				brackets.push(bracketType);
				level++;
			} else if ((bracketType = ")]}".indexOf(currentChar)) >= 0) {
				if (bracketType == brackets.peek()) {
					brackets.pop();
					level--;
				}
			}
			lastChar = currentChar;
		}
		ArrayList<String> results = new ArrayList<>();
		int lastIndex = 0;
		for (int i : indices)
			results.add(regex.substring(lastIndex, (lastIndex = i) - 2));
		results.add(regex.substring(lastIndex));
		return results;
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
		MCLogLine foundLine;
		int matchIndex = -1;
		for (int i = 0; i < extractedFilters.size(); i++)
			if (extractedFilters.get(i) != null && extractedFilters.get(i).get(0) != null
					&& line.getText().matches(extractedFilters.get(i).get(0)))
				matchIndex = i;
		ArrayList<String> subFilters;
		if (matchIndex >= 0) {
			subFilters = extractedFilters.get(matchIndex);
			foundLine = line;
			for (int i = 1; i < subFilters.size(); i++) {
				for (int j = 0; j < 20; j++) {
					if (lineIndex >= logLines.size() - 1)
						return;
					line = logLines.get(lineIndex = (lineIndex < logLines.size() - 1) ? (lineIndex + 1) : lineIndex);
					if (line.getText().matches(subFilters.get(0))) {
						return;
					} else if (line.getText().matches(subFilters.get(i))) {
						foundLine.appendText(" " + line.getText());
						break;
					} else if (lineIndex > recordStartLine + 20) {
						return;
					}
				}
			}
			add(foundLine);
		}
		return;
	}

	public String getLineFilter() {
		return lineFilter;
	}

}
