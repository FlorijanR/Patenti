package hr.fer.patenti.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bitcoinj.core.Base58;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.qos.logback.core.net.SyslogOutputStream;
import hr.fer.patenti.dao.ResultRepository;
import hr.fer.patenti.domain.Result;
import hr.fer.patenti.rest.PatentDTO;
import hr.fer.patenti.service.MyService;

@Service
public class MyServiceJPA implements MyService {
	@Autowired
	private ResultRepository resultRepo;

	@Override
	public Result req(PatentDTO info) {
		Result res = new Result();
		res.setHash(solana(info));

		hyperledgerFabric(info);

		String model = info.getModelName();
		String address = "http://127.0.0.1:5000/";
		if (model.equalsIgnoreCase("prvi")) {
			address = "http://127.0.0.1:5000/";
		}
		if (model.equalsIgnoreCase("drugi")) {
			address = "http://127.0.0.1:5001/";
		}

		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(address);

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("patentText", info.getPatentText()));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Execute and get the response.
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			try (InputStream instream = entity.getContent()) {
				int bufferSize = 1024;
				char[] buffer = new char[bufferSize];
				StringBuilder out = new StringBuilder();
				Reader in = new InputStreamReader(instream, StandardCharsets.UTF_8);
				for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0;) {
					out.append(buffer, 0, numRead);
				}
				res.setGroup(out.toString());
				resultRepo.save(res);
				return res;
			} catch (UnsupportedOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private void hyperledgerFabric(PatentDTO info) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] encodedhash = digest.digest(info.getPatentText().getBytes(StandardCharsets.UTF_8));
		String patentHash = bytesToHex(encodedhash);
		patentHash = calculatePatentHash(patentHash);

		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");

		// Load a file system based wallet for managing identities.
		Path walletPath = Paths.get("C:\\fabric-samples-repo\\3\\fabric-samples\\fabcar\\java\\wallet");
		Wallet wallet = null;
		try {
			wallet = Wallets.newFileSystemWallet(walletPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// load a CCP
		Path networkConfigPath = Paths.get(
				"C:\\fabric-samples-repo\\3\\fabric-samples\\test-network\\organizations\\peerOrganizations\\org1.example.com\\connection-org1.yaml");

		Gateway.Builder builder = Gateway.createBuilder();
		try {
			builder.identity(wallet, "appUser").networkConfig(networkConfigPath).discovery(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try (Gateway gateway = builder.connect()) {
			
			// get the network and contract
			Network network = gateway.getNetwork("mychannel");
			Contract contract = network.getContract("fabcar");
			
			byte[] result = null;
			
			try {
				contract.submitTransaction("createCar", patentHash, patentHash, "", "", "");
			} catch (ContractException | TimeoutException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				result = contract.evaluateTransaction("queryCar", patentHash);
			} catch (ContractException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(new String(result));
			
		}

	}

	private String solana(PatentDTO info) {

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] encodedhash = digest.digest(info.getPatentText().getBytes(StandardCharsets.UTF_8));
		String patentHash = bytesToHex(encodedhash);
		patentHash = calculatePatentHash(patentHash);

		RpcClient client = new RpcClient("https://api.testnet.solana.com");

		PublicKey fromPublicKey = new PublicKey("BgGL1Zbs16coZypgFbo1QH32axJYgkcyfuYuYwGEqRXF");
		// PublicKey toPublickKey = new
		// PublicKey("9rATiKbk6hWm2RnYpcX9UNmyjZ7XL77CSvVNbqdhHgJp");
		PublicKey toPublickKey = new PublicKey(patentHash);
		int lamports = 1_000_000; // 1_000_000_000 = 1 sol

		Account signer = new Account(Base58
				.decode("5V6HFgkzdVqcafo5Vu4i5J1RrXs72eJ1gHbjQ4djBkKuqvtuBiN3pu38Sr7S3UvxfCKxJA9qeVn8eMWKbkXN8zuw"));

		Transaction transaction = new Transaction();
		transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));

		String signature = null;
		try {
			signature = client.getApi().sendTransaction(transaction, signer);
		} catch (RpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return signature;
	}

	private String calculatePatentHash(String patentHash) {
		// System.out.println(patentHash);
		patentHash = patentHash.substring(0, 43);
		patentHash = patentHash.replaceAll("0", "A");
		patentHash = patentHash.replaceAll("I", "B");
		patentHash = patentHash.replaceAll("O", "C");
		patentHash = patentHash.replaceAll("l", "D");
		patentHash = "2" + patentHash;

		System.out.println(patentHash);
		return patentHash;
		// return Base58.decode(Base58.encode(patentHash.getBytes())).toString();
	}

	private static String bytesToHex(byte[] hash) {
		StringBuilder hexString = new StringBuilder(2 * hash.length);
		for (int i = 0; i < hash.length; i++) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
