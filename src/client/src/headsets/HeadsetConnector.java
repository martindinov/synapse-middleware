package headsets;

import interfaces.MqttMessageReceiver;
import core.HeadsetInfo;

public interface HeadsetConnector  {
	public HeadsetInfo getHeadsetInfo();
}
