package evidence.qiwi;

import com.qiwi.billpayments.sdk.client.BillPaymentClient;
import com.qiwi.billpayments.sdk.client.BillPaymentClientFactory;
import com.qiwi.billpayments.sdk.model.MoneyAmount;
import com.qiwi.billpayments.sdk.model.in.PaymentInfo;
import lombok.Data;

import java.io.Reader;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.logging.Logger;

@Data
public class PaymentHandler {

	private final Logger logger;
	private final String publicKey;
	private final String secretKey;
	private final BillPaymentClient client;

	public PaymentHandler(Logger logger, String publicKey, String secretKey) {
		this.logger = logger;
		this.publicKey = publicKey;
		this.secretKey = secretKey;
		this.client = BillPaymentClientFactory.createDefault(secretKey);
	}

	public String createPaymentForm(double price, String successUrl) {
		MoneyAmount amount = new MoneyAmount(
				BigDecimal.valueOf(price),
				Currency.getInstance("RUB")
		);
		String billId = UUID.randomUUID().toString();
		return client.createPaymentForm(new PaymentInfo(publicKey, amount, billId, successUrl));
	}

	public void handleEvent(Reader reader) {
		
	}

}
