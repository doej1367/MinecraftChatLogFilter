package util;

/**
 *
 * @author doej1367
 */
public class MCLogLine implements Comparable<MCLogLine> {
	private long creationTime;
	private String playerName;
	private String text;

	public MCLogLine(long creationTime, String playerName, String text, boolean stripColorCodes) {
		this.creationTime = creationTime;
		this.playerName = playerName;
		this.text = stripColorCodes ? text.replaceAll("§.", "") : text;
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getText() {
		return text;
	}

	public void appendText(String additionalText) {
		text += additionalText;
	}

	public int matches(String[] regexArray) {
		for (int i = 0; i < regexArray.length; i++)
			if (getText().matches(regexArray[i]))
				return i;
		return -1;
	}

	@Override
	public int compareTo(MCLogLine arg0) {
		long tmp = getCreationTime() - arg0.getCreationTime();
		return tmp < 0 ? -1 : tmp > 0 ? 1 : 0;
	}
}
