<script>
  import { mowline } from '$scripts/mowline.js';
  import { safehtml } from '$lib/safehtml.js';
  import './page.css';

  let { data } = $props();
  const meta = $derived(data.meta ?? {});

  // Fallback facts if the portal hasn't populated serviceHeader yet.
  const fallbackFacts = [
    { header: 'Founded',        section: '2015' },
    { header: 'Crew',           section: 'Four, plus two trucks' },
    { header: 'Service area',   section: 'Greater Louisville' },
    { header: 'Subcontractors', section: 'None' }
  ];

  // Prefer portal-managed facts; fall back to the static set when empty.
  const facts = $derived(
    meta.serviceHeader?.length ? meta.serviceHeader : fallbackFacts
  );

  // The three prose sections in render order.
  const sections = $derived(
    [meta.topSection, meta.midSection, meta.bottomSection].filter(
      (s) => s && (s.header || s.section)
    )
  );
</script>

<svelte:head>
  <title>About | Exceptional Landscaping &amp; Lawn Services</title>
  <meta name="description" content="Exceptinal Landscaping &amp; Lawn Services is a two-person, family owned landscaping crew working across greater Louisville since 2022." />
</svelte:head>

<article class="about">
  <header class="about__head shell">
    <span class="eyebrow">About</span>
    <h1 class="about__title">{@html safehtml(meta.aboutHeader ?? 'Eleven years of reading slopes')}</h1>
  </header>

  <div class="about__facts-band">
    <dl class="about__facts shell">
      {#each facts as fact}
        <div class="about__fact">
          <dt class="about__fact-label">{@html safehtml(fact.header)}</dt>
          <dd class="about__fact-value">{@html safehtml(fact.section)}</dd>
        </div>
      {/each}
    </dl>
  </div>

  <div class="about__body shell">
    <div class="about__prose">
      {#each sections as sec, i}
        {#if sec.header}
          <h2 class="about__h2" class:about__lede={i === 0 && !sec.section}>{@html safehtml(sec.header)}</h2>
        {/if}
        {#if sec.section}
          <p class:about__lede={i === 0}>{@html safehtml(sec.section)}</p>
        {/if}
        {#if i < sections.length - 1}
          <div class="mowline about__rule" use:mowline></div>
        {/if}
      {/each}

      <div class="about__cta">
        <a href="/contact" class="btn btn--solid">Request lawn service now</a>
        <a href="/gallery" class="btn">See our work first</a>
      </div>
    </div>

    <aside class="about__aside">
      {#if meta.aboutImageUrl}
        <img
          class="about__photo about__photo--img"
          src={meta.aboutImageUrl}
          alt={meta.aboutImageAltText ?? meta.aboutImageCaption ?? 'Exceptional Landscaping crew'}
          loading="lazy"
          decoding="async"
        />
      {:else}
        <div class="about__photo"></div>
      {/if}
      {#if meta.aboutImageCaption}
        <p class="about__caption">{meta.aboutImageCaption}</p>
      {/if}

      <div class="about__contact">
        <span class="about__contact-label">Call the office</span>
        <a href="tel:+15025550142" class="about__phone">(502) 202-5739</a>
        <p class="about__hours">Monday to Friday, 7am to 5pm</p>
      </div>
    </aside>
  </div>
</article>