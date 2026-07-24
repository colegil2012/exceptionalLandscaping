/**
 * scripts/mowline.js
 *
 * Svelte action powering the site's signature device: a rule that sweeps
 * across as its section enters the viewport, like a mower pass.
 *
 * Usage:  <div class="mowline" use:mowline></div>
 *
 * Uses IntersectionObserver and disconnects after the first sweep — the line
 * is a one-time arrival, not a scroll-linked effect that replays.
 */

export function mowline(node, options = {}) {
  const { threshold = 0.6, delay = 0 } = options;

  // Without IntersectionObserver, show the swept state immediately rather
  // than leaving the line permanently unfinished.
  if (typeof IntersectionObserver === 'undefined') {
    node.classList.add('mowline--swept');
    return {};
  }

  let timer;

  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          timer = setTimeout(() => node.classList.add('mowline--swept'), delay);
          observer.disconnect();
        }
      }
    },
    { threshold }
  );

  observer.observe(node);

  return {
    destroy() {
      clearTimeout(timer);
      observer.disconnect();
    }
  };
}
