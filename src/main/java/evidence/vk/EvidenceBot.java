package evidence.vk;

import com.sun.net.httpserver.HttpServer;
import evidence.Evidence;
import evidence.EvidenceManager;
import evidence.qiwi.PaymentHandler;
import implario.Environment;
import implario.vk.GroupSession;
import implario.vk.VkClient;
import implario.vk.event.callback.CallbackEventListener;
import implario.vk.event.events.messages.NewMessageEvent;
import implario.vk.model.message.Attachment;
import implario.vk.model.message.Message;
import implario.vk.model.message.OutcomingMessage;
import implario.vk.model.message.attachment.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static implario.LoggerUtils.simpleLogger;

public class EvidenceBot {

	public static VkClient client;
	public static EvidenceManager evidenceManager = new EvidenceManager();

	public static void main(String[] args) throws Exception {
		evidenceManager.init(simpleLogger("Evidence"));
		client = new VkClient(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build());
		GroupSession session = client.newGroupSession(
				Environment.requireInt("VK_BOT_ID"),
				Environment.require("VK_BOT_TOKEN")
													 );

		HttpServer server = HttpServer.create(new InetSocketAddress(Environment.getInt("EVIDENCE_PORT", 80)), 0);

		CallbackEventListener listener = session.createCallbackListener(simpleLogger("Callback"), "hn209y845ki8w9uyd8dlou0e5");

		PaymentHandler paymentHandler = new PaymentHandler(simpleLogger("QIWI"),
				Environment.require("QIWI_PUBLIC_KEY"), Environment.require("QIWI_SECRET_KEY"));

		System.out.println(paymentHandler.createPaymentForm(1, "https://vk.com"));
		if (true) return;

		server.createContext("/vk", exchange -> {
			InputStream requestBody = exchange.getRequestBody();
			listener.handleEvent(new InputStreamReader(requestBody));
			String response = "OK";
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});

		server.createContext("/billing", exchange -> {
			InputStream requestBody = exchange.getRequestBody();
			paymentHandler.handleEvent(new InputStreamReader(requestBody));
			String response = "OK";
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();
		});
		server.start();

		listener.start();
		listener.registerHandler(NewMessageEvent.class, event -> {
			Message message = event.getMessage();
			Attachment[] attachments = message.getAttachments();
			if (attachments != null && attachments.length != 0) {
				String configUrl = null;
				String screenshotUrl = null;
				int peerId = message.getPeerId();
				for (Attachment attachment : attachments) {
					if (attachment instanceof Document) {
						String extension = ((Document) attachment).getExtension();
						String url = ((Document) attachment).getUrl();
						if (extension.equals("yml")) {
							if (configUrl == null) configUrl = url;
							else {
								session.sendMessage(new OutcomingMessage("Вы прикрепили больше одного .yml файла"), peerId);
								return;
							}
						} else if (extension.equals("png")) {
							if (screenshotUrl == null) screenshotUrl = url;
							else {
								session.sendMessage(new OutcomingMessage("За раз можно прикреплять только один скриншот"), peerId);
								return;
							}
						}
					}
				}
				if ((configUrl == null || screenshotUrl == null) && !Objects.equals(configUrl, screenshotUrl)) {
					session.sendMessage(new OutcomingMessage("Нужно прикреплять сразу и скриншот и evidence.yml"), peerId);
					return;
				}

				try {
					byte[] generate = new Evidence(evidenceManager).generate(download(configUrl), download(screenshotUrl), false);
					CompletableFuture<Attachment> attachmentCompletableFuture = session.uploadDocument(peerId, "test.png", "image/png", generate);
					System.out.println(attachmentCompletableFuture.thenCompose(attach ->
							session.sendMessage(new OutcomingMessage("Here you go:").attach(attach.toOutcoming()), peerId)).join());
				} catch (Exception ex) {
					session.sendMessage(new OutcomingMessage("Во время генерации подставы произошла ошибка"), peerId);
					ex.printStackTrace();
				}
			}
		});
		while (true) Thread.sleep(1000);
	}

	public static InputStream download(String url) throws IOException, InterruptedException {
		HttpResponse<InputStream> send = client.getHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
				HttpResponse.BodyHandlers.ofInputStream());
		return send.body();
	}

}
