package tahrir.io.net.udp;

import java.net.DatagramPacket;

public class PrioritizedUdpPacket implements Comparable<PrioritizedUdpPacket> {

	public final DatagramPacket packet;
	public final double priority;
	public final SentListener sentListener;

	public PrioritizedUdpPacket(final DatagramPacket packet, final double priority) {
		this(packet, priority, null);
	}

	public PrioritizedUdpPacket(final DatagramPacket packet, final double priority, final SentListener sentListener) {
		this.packet = packet;
		this.priority = priority;
		this.sentListener = sentListener;
	}

	public int compareTo(final PrioritizedUdpPacket o) {
		// We reverse this because the *lowest* comes out first in a
		// PriorityQueue
		return 1 - Double.compare(priority, o.priority);
	}

	public static interface SentListener {
		public void sent();
	}

	public static final double CONNECTION_MAINTAINANCE = 100;
}