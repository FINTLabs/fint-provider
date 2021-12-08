package no.fint.provider.events.sse;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.time.DatePrinter;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FintSseEmitter extends SseEmitter {
    private String id;
    private String client;
    private String registered;
    private Set<String> actions = new HashSet<>();
    private final AtomicInteger eventCounter = new AtomicInteger();
    private static final DatePrinter DATE_PRINTER = FastDateFormat.getInstance("HH:mm:ss dd/MM/yyyy");

    public FintSseEmitter() {
        setRegisteredDate();
    }

    public FintSseEmitter(String id, String client, long timeout) {
        super(timeout);
        this.id = id;
        this.client = client;
        setRegisteredDate();
    }

    private void setRegisteredDate() {
        registered = DATE_PRINTER.format(new Date());
    }

	@Override
	public void send(SseEventBuilder builder) throws IOException {
		eventCounter.incrementAndGet();
		super.send(builder);
	}
    
    
}
