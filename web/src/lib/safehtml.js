/**
 * Minimal allow-list HTML sanitizer for portal-managed copy.
 *
 * The about-page text comes from the Celtech admin portal and may contain a few
 * formatting tags (a <br> to control wrapping, some emphasis). We render that
 * with {@html}, which would otherwise be an XSS vector, so we strip everything
 * except a small whitelist of inline tags and drop all attributes.
 *
 * Runs in both browser (DOMParser) and SSR (regex fallback) so server-rendered
 * output is sanitized too.
 */
const ALLOWED = new Set(['br', 'strong', 'b', 'em', 'i', 'u', 'span', 'sub', 'sup']);

/** Decode the handful of entities the portal is likely to escape, so an
 *  escaped "&lt;br&gt;" becomes a real "<br>" before we sanitize it. */
function decodeEntities(s) {
    return s
        .replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&quot;/g, '"')
        .replace(/&#39;/g, "'")
        .replace(/&amp;/g, '&'); // do & last so we don't double-decode
}

export function safehtml(input) {
    if (!input) return '';
    const raw = decodeEntities(String(input));

    // Server-side (no DOM): strip any tag not in the allow-list, and drop all
    // attributes from allowed tags. Good enough for trusted, simple markup.
    if (typeof document === 'undefined') {
        return raw.replace(/<\/?([a-zA-Z0-9]+)(\s[^>]*)?>/g, (match, tag) => {
            const name = tag.toLowerCase();
            if (!ALLOWED.has(name)) return '';
            // Rebuild the tag without any attributes.
            return match.startsWith('</') ? `</${name}>` : `<${name}>`;
        });
    }

    // Browser: parse into a detached document and walk it, keeping only allowed
    // elements (unwrapping disallowed ones) and stripping every attribute.
    const doc = new DOMParser().parseFromString(raw, 'text/html');

    const clean = (node) => {
        [...node.childNodes].forEach((child) => {
            if (child.nodeType === Node.ELEMENT_NODE) {
                const name = child.tagName.toLowerCase();
                if (!ALLOWED.has(name)) {
                    // Unwrap: replace the disallowed element with its (cleaned) contents.
                    clean(child);
                    child.replaceWith(...child.childNodes);
                    return;
                }
                // Strip all attributes from allowed elements.
                [...child.attributes].forEach((attr) => child.removeAttribute(attr.name));
                clean(child);
            }
        });
    };

    clean(doc.body);
    return doc.body.innerHTML;
}