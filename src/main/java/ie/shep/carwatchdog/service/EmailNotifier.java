package ie.shep.carwatchdog.service;

import ie.shep.carwatchdog.domain.SearchDefinition;
import ie.shep.carwatchdog.domain.SeenAd;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailNotifier {

    private final JavaMailSender mailSender;

    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendDriftEmail(
            SearchDefinition search,
            List<SeenAd> added,
            List<SeenAd> removed
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(search.getNotifyEmail());
            helper.setSubject("Car watch drift: "
                    + added.size() + " added, "
                    + removed.size() + " removed");

            StringBuilder body = new StringBuilder();
            body.append("<h2>Car search drift detected</h2>");
            body.append("<p><b>").append(escape(search.getName())).append("</b></p>");
            body.append("<p>Added: ").append(added.size())
                    .append("<br>Removed: ").append(removed.size()).append("</p>");

            if (!added.isEmpty()) {
                body.append("<h3>New ads</h3><ul>");
                for (SeenAd ad : added) {
                    body.append("<li><a href=\"")
                            .append(escape(ad.getUrl()))
                            .append("\">")
                            .append(escape(ad.getSource() + " #" + ad.getExternalId()))
                            .append("</a>");

                    if (ad.getFirstImageUrl() != null) {
                        body.append("<br><img src=\"")
                                .append(escape(ad.getFirstImageUrl()))
                                .append("\" width=\"220\" alt=\"listing image\">");
                    }

                    body.append("</li>");
                }
                body.append("</ul>");
            }

            if (!removed.isEmpty()) {
                body.append("<h3>Removed ads</h3><ul>");
                for (SeenAd ad : removed) {
                    body.append("<li><a href=\"")
                            .append(escape(ad.getUrl()))
                            .append("\">")
                            .append(escape(ad.getSource() + " #" + ad.getExternalId()))
                            .append("</a></li>");
                }
                body.append("</ul>");
            }

            helper.setText(body.toString(), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to send drift email", e);
        }
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
