<script>
  import './ServiceCard.css';

  /**
   * Homepage highlight card. Signature interaction: a band of cut-green
   * sweeps up from the base on hover, the way a mower pass fills in a strip.
   */
  let { href, eyebrow, title, blurb, cta, index = 0 } = $props();

  let hovered = $state(false);
</script>

<a
  {href}
  class="scard"
  class:scard--hovered={hovered}
  style="--card-index: {index}"
  onmouseenter={() => (hovered = true)}
  onmouseleave={() => (hovered = false)}
  onfocusin={() => (hovered = true)}
  onfocusout={() => (hovered = false)}
>
  <span class="scard__sweep" aria-hidden="true"></span>

  <div class="scard__body">
    <span class="eyebrow scard__eyebrow">{eyebrow}</span>
    <h3 class="scard__title">{title}</h3>
    <p class="scard__blurb">{blurb}</p>
    <span class="scard__cta">
      {cta}
      <svg class="scard__arrow" viewBox="0 0 24 12" aria-hidden="true">
        <path d="M0 6h21M16 1l5 5-5 5" fill="none" stroke="currentColor" stroke-width="2" />
      </svg>
    </span>
  </div>
</a>
