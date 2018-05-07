package interfaces;

public interface CoreServerInterface {
	public boolean acceptConnection(String client);
	public String getFormattedDataSample(String[] clients, int timeInterval);
}
