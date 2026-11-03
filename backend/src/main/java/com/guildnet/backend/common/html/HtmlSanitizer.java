package com.guildnet.backend.common.html;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.util.regex.Pattern;

/**
 * Limpia el HTML generado por el editor Quill antes de guardarlo, para que no se pueda
 * inyectar código (XSS) en publicaciones, descripciones, reglas o comentarios.
 * Se conservan las etiquetas de formato que produce Quill y sus clases {@code ql-*}.
 */
public final class HtmlSanitizer {

    private static final Pattern QUILL_CLASS = Pattern.compile("(\\s*ql-[a-z0-9-]+\\s*)+");

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.STYLES)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(new HtmlPolicyBuilder()
                    .allowElements("pre", "code", "span", "br", "hr", "s", "u", "sub", "sup")
                    .allowAttributes("class").matching(QUILL_CLASS).globally()
                    .allowAttributes("data-list").onElements("li")
                    .allowAttributes("spellcheck").onElements("pre")
                    .requireRelNofollowOnLinks()
                    .toFactory());

    private HtmlSanitizer() {
    }

    /** Devuelve el HTML limpio, o {@code null} si la entrada es {@code null}. */
    public static String sanitize(String html) {
        return html == null ? null : POLICY.sanitize(html);
    }
}
