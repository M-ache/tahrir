package tahrir.io.net.sessions;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import org.testng.Assert;
import org.testng.annotations.Test;

import tahrir.*;
import tahrir.io.net.*;
import tahrir.io.net.udpV1.UdpNetworkLocation;
import tahrir.tools.*;

public class AssimilateTest {
	@Test(enabled=true)
	public void twoPeerTest() throws Exception {
		System.out.println("Joiner (7644) will assimilate to seed (7643)");

		final TrConfig seedConfig = new TrConfig();
		seedConfig.capabilities.allowsAssimilation = true;
		seedConfig.capabilities.allowsUnsolicitiedInbound = true;
		seedConfig.peers.assimilate = false;
		seedConfig.localHostName = "localhost";
		seedConfig.udp.listenPort = 7643;
		final File seedDir = TrUtils.createTempDirectory();
		final TrNode seedNode = new TrNode(seedDir, seedConfig);
		final RemoteNodeAddress seedPublicNodeId = seedNode.getRemoteNodeAddress();

		final File joinerDir = TrUtils.createTempDirectory();

		final TrConfig joinerConfig = new TrConfig();

		joinerConfig.udp.listenPort = 7644;
		joinerConfig.localHostName = "localhost";
		joinerConfig.peers.assimilate = true;

		final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

		joinerPubNodeIdsDir.mkdir();

		// Ok, we should be getting this TrPeerInfo out of seedNode somehow rather
		// than needing to set its capabilities manually like this
		final TrPeerManager.TrPeerInfo seedPeerInfo = new TrPeerManager.TrPeerInfo(seedPublicNodeId);
		seedPeerInfo.capabilities = seedConfig.capabilities;

		Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedPeerInfo);

		final TrNode joinerNode = new TrNode(joinerDir, joinerConfig);

		for (int x=0; x<50; x++) {
			Thread.sleep(200);
			if (joinerNode.peerManager.peers.containsKey(seedNode.getRemoteNodeAddress().location)
					&& seedNode.peerManager.peers.containsKey(joinerNode.getRemoteNodeAddress().location)) {
				break;
			}
		}
		// Verify that they are now connected
		Assert.assertTrue(joinerNode.peerManager.peers.containsKey(seedNode.getRemoteNodeAddress().location), "The joiner peer manager should contain the seed peer");
		Assert.assertTrue(seedNode.peerManager.peers.containsKey(joinerNode.getRemoteNodeAddress().location), "The seed peer manager should contain the joiner peer");

	}

	@Test(dependsOnMethods = {"twoPeerTest"})
	public void multiPeerTest() throws Exception {
		final TrConfig seedConfig = new TrConfig();
		seedConfig.capabilities.allowsAssimilation = true;
		seedConfig.capabilities.allowsUnsolicitiedInbound = true;
		seedConfig.peers.assimilate = false;
		seedConfig.peers.maxPeers = 5;
		seedConfig.peers.minPeers = 2;
		seedConfig.localHostName = "localhost";
		seedConfig.udp.listenPort = 5648;
		final File seedDir = TrUtils.createTempDirectory();
		final TrNode seedNode = new TrNode(seedDir, seedConfig);
		final RemoteNodeAddress seedPublicNodeId = seedNode.getRemoteNodeAddress();

		final List<TrNode> joiners = Lists.newLinkedList();

		for (int x=0; x<100; x++) {
			Thread.sleep(500);
			final File joinerDir = TrUtils.createTempDirectory();

			final TrConfig joinerConfig = new TrConfig();

			joinerConfig.udp.listenPort = 5650+x;
			joinerConfig.localHostName = "localhost";
			joinerConfig.peers.assimilate = true;
			joinerConfig.peers.maxPeers = 5;
			joinerConfig.peers.minPeers = 2;
			final File joinerPubNodeIdsDir = new File(joinerDir, joinerConfig.publicNodeIdsDir);

			joinerPubNodeIdsDir.mkdir();

			// Ok, we should be getting this TrPeerInfo out of seedNode somehow rather
			// than needing to set its capabilities manually like this
			final TrPeerManager.TrPeerInfo seedPeerInfo = new TrPeerManager.TrPeerInfo(seedPublicNodeId);
			seedPeerInfo.capabilities = seedConfig.capabilities;

			Persistence.save(new File(joinerPubNodeIdsDir, "joiner-id"), seedPeerInfo);

			joiners.add(new TrNode(joinerDir, joinerConfig));
		}

		Thread.sleep(20000);

		dumpVertex(seedNode);
		for (final TrNode joiner : joiners) {
			dumpVertex(joiner);
		}
	}

	private void dumpVertex(final TrNode node) {
		for (final PhysicalNetworkLocation o : node.peerManager.peers.keySet()) {
			System.out.print(((UdpNetworkLocation)node.getRemoteNodeAddress().location).port + " -> "+((UdpNetworkLocation) o).port+", ");
		}
	}
}
