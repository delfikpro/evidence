package evidence.vk;

import com.qiwi.billpayments.sdk.model.BillStatus;
import com.qiwi.billpayments.sdk.model.out.BillResponse;
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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static implario.LoggerUtils.simpleLogger;

public class EvidenceBot {

	public static VkClient client;
	public static EvidenceManager evidenceManager = new EvidenceManager();
	public static BillStorage billStorage = new BillStorage();
	public static Set<Integer> privilegedUsers = new HashSet<>();

	public static void main(String[] args) throws Exception {

		for (String privilegedUser : Environment.get("PRIVILEGED_USERS", "0").split(",")) {
			privilegedUsers.add(Integer.parseInt(privilegedUser));
		}

		System.out.println("Loading...");
		evidenceManager.init(simpleLogger("Evidence"));
		client = new VkClient(HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build());
		int vkBotId = Environment.requireInt("VK_BOT_ID");
		GroupSession session = client.newGroupSession(
				vkBotId,
				Environment.require("VK_BOT_TOKEN")
													 );

		HttpServer server = HttpServer.create(new InetSocketAddress(Environment.getInt("EVIDENCE_PORT", 80)), 0);

		CallbackEventListener listener = session.createCallbackListener(simpleLogger("Callback"), Environment.get("VK_CALLBACK_SECRET"));

		PaymentHandler paymentHandler = new PaymentHandler(simpleLogger("QIWI"),
				Environment.require("QIWI_PUBLIC_KEY"), Environment.require("QIWI_SECRET_KEY"));

		server.createContext("/pay", exchange -> {
			try {
				EvidenceBill bill = billStorage.getBill(fromBase64(exchange.getRequestURI().getQuery() + "=="));
				exchange.getResponseHeaders().put("location", Collections.singletonList(bill != null ? bill.getLink() : "https://vk.me/evime"));
				exchange.sendResponseHeaders(302, 0);
				exchange.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				exchange.sendResponseHeaders(404, 0);
				exchange.close();
			}
		});
		
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
			BillResponse billInfo = paymentHandler.read(new InputStreamReader(requestBody));
			String response = "OK";
			exchange.sendResponseHeaders(200, response.length());
			OutputStream os = exchange.getResponseBody();
			os.write(response.getBytes());
			os.close();

			if (billInfo.getStatus().getValue() == BillStatus.PAID) {
				EvidenceBill bill = billStorage.getBill(UUID.fromString(billInfo.getBillId()));
				session.getMessageById((int) bill.getMessageId()).thenAccept(messages -> {
					if (messages.length == 0) {
						session.sendMessage(new OutcomingMessage("Не удаётся получить доступ к пересланному сообщению"), bill.getPeerId());
						return;
					}
					if (bill.getPrice() != billInfo.getAmount().getValue().doubleValue() || !billInfo.getAmount().getCurrency().getCurrencyCode().equals("RUB")) {
						session.sendMessage(new OutcomingMessage("Ты что оплатил счёт не на 25 рублей.... " + billInfo.getAmount()), bill.getPeerId());
						return;
					}
					messages[0].setPeerId(bill.getPeerId());
					processMessage(session, messages[0], false);
					bill.setPaid(true);
				});
			}

		});
		server.start();

		int price = Environment.requireInt("SCREENSHOT_PRICE");

		listener.start();
		listener.registerHandler(NewMessageEvent.class, event -> {
			Message message = event.getMessage();
			String text = message.getText();
			if (text.equalsIgnoreCase("Начать") || text.equalsIgnoreCase("Старт") || text.equalsIgnoreCase("/start") ||
			text.equalsIgnoreCase("/help")) {

				try {
					CompletableFuture<Attachment> attachmentCompletableFuture = session.uploadDocument(message.getPeerId(), "evidence.yml",
							"text/plain", Files.readAllBytes(new File("default-evidence.yml").toPath()));
					attachmentCompletableFuture.thenAccept(attach ->
							session.sendMessage(new OutcomingMessage("Бип-буп? Приветствую!\n" +
									"\n" +
									"Для дальнейшей работы со мной и последующей генерации скриншота(-ов) VimeWorld тебе следует загрузить на свой компьютер конфигурационный файл .yml и с помощью одного из текстовых редакторов настроить его под себя.\n" +
									"\n" +
									"Как только ты это сделаешь, сразу же беги отправлять мне в этот диалог скриншот (Документом) с включенным F1, а вместе с ним и этот файл. Что дальше? А дальше я незамедлительно сгенерирую тебе подставу :)\n" +
									"\n" +
									"Все свои скриншоты ты с легкостью сможешь найти в папке «.vimeworld» → «minigames» → «screenshots»"
							).attach(attach.toOutcoming()).setReplyTo(message.getId()), message.getPeerId()));
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
			if (text.equals("+")) {
				Message repliedMessage = message.getRepliedMessage();
				if (repliedMessage == null) {
					System.out.println("jthutiruojhkdgfjdgksjnb");
					return;
				}
				session.getMessageById((int) repliedMessage.getId()).thenAccept(messages -> {
					if (messages.length == 0) {
						session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Не удаётся получить доступ к пересланному сообщению"), message.getPeerId());
						return;
					}
					Message msg = messages[0].getRepliedMessage();
					if (messages[0].getFromId() != -vkBotId || msg.getText().contains("Оплата прошла успешно")) {
						System.out.println(messages[0].getFromId() + " " + vkBotId + " " + msg.getText());
						return;
					}
					UUID uuid = UUID.randomUUID();
					String successUrl = Environment.get("SUCCESS_URL", "https://vk.com");
					String paymentLink = paymentHandler.createPaymentForm(price, uuid, successUrl);

					EvidenceBill bill = new EvidenceBill(message.getPeerId(), msg.getId(), uuid, price, paymentLink);
					billStorage.saveBill(bill);


					System.out.println(paymentLink);

					String serializedId = toBase64(bill.getUuid()).replace("=", "");
					session.sendMessage(new OutcomingMessage("Для того, чтобы убрать вотермарки и получить полную версию скриншота, следует оплатить покупку по ссылке ниже:\n" +
							"evidence.implario.dev/pay?" + serializedId), message.getPeerId());
				}).exceptionally(ex -> {
					session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Произошла неизвестная ошибка!"), message.getPeerId());
					ex.printStackTrace();
					return null;
				});
			}
			if (message.getAttachments() != null && message.getAttachments().length != 0) {
				processMessage(session, message, price != 0 && !privilegedUsers.contains(message.getFromId()));
			}
		});

		System.out.println("Ready.");
		while (true) Thread.sleep(1000);
	}

	public static UUID fromBase64(String base64) {
		ByteBuffer wrap = ByteBuffer.wrap(Base64.getUrlDecoder().decode(base64));
		LongBuffer longBuffer = wrap.asLongBuffer();
		return new UUID(longBuffer.get(), longBuffer.get());
	}

	public static String toBase64(UUID uuid) {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		LongBuffer longBuffer = buffer.asLongBuffer();
		longBuffer.put(uuid.getMostSignificantBits());
		longBuffer.put(uuid.getLeastSignificantBits());
		return Base64.getUrlEncoder().encodeToString(buffer.array());
	}

	private static void processMessage(GroupSession session, Message message, boolean watermark) {
		System.out.println("Processing " + message);
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
							session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Вы прикрепили больше одного .yml файла"), peerId);
							return;
						}
					} else if (extension.equals("png")) {
						if (screenshotUrl == null) screenshotUrl = url;
						else {
							session.sendMessage(new OutcomingMessage("\uD83D\uDEAB За раз можно прикреплять только один скриншот"), peerId);
							return;
						}
					}
				}
			}
			if ((configUrl == null || screenshotUrl == null) && !Objects.equals(configUrl, screenshotUrl)) {
				session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Нужно прикреплять сразу и скриншот и evidence.yml"), peerId);
				return;
			}

			try {
				byte[] generate = new Evidence(evidenceManager).generate(download(configUrl), download(screenshotUrl), false, watermark);
				CompletableFuture<Attachment> attachmentCompletableFuture = session.uploadDocument(peerId, "test.png", "image/png", generate);
				System.out.println(attachmentCompletableFuture.thenCompose(attach ->
						session.sendMessage(new OutcomingMessage(
//									"Ссылка для оплаты: " + paymentHandler.createPaymentForm(price, "https://vk.com")
										watermark ? "" : "\uD83D\uDCB8 Оплата прошла успешно."
						).attach(attach.toOutcoming()).setReplyTo(message.getId()), peerId)).join());
			} catch (Exception ex) {
				session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Во время генерации подставы произошла ошибка"), peerId);
				ex.printStackTrace();
			}
		} else {
			session.sendMessage(new OutcomingMessage("\uD83D\uDEAB Произошла странная ошибка с вложениями"), message.getPeerId());
		}
	}

	public static InputStream download(String url) throws IOException, InterruptedException {
		HttpResponse<InputStream> send = client.getHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
				HttpResponse.BodyHandlers.ofInputStream());
		return send.body();
	}

}
