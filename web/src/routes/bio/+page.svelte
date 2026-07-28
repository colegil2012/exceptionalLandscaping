<script>
  import { mowline } from '$scripts/mowline.js';
  import './page.css';

  const facts = [
    { label: 'Founded',        value: '2015' },
    { label: 'Crew',           value: 'Four, plus two trucks' },
    { label: 'Service area',   value: 'Greater Louisville' },
    { label: 'Subcontractors', value: 'None' }
  ];
</script>

<svelte:head>
  <title>About — Exceptional Landscaping &amp; Lawn Services</title>
  <meta name="description" content="Exceptinal Landscaping &amp; Lawn Services is a two-person, family owned landscaping crew working across greater Louisville since 2022." />
</svelte:head>

<article class="about">
  <header class="about__head shell">
    <span class="eyebrow">About</span>
    <h1 class="about__title">Eleven years of<br />reading slopes</h1>
  </header>

  <div class="about__facts-band">
    <dl class="about__facts shell">
      {#each facts as fact}
        <div class="about__fact">
          <dt class="about__fact-label">{fact.label}</dt>
          <dd class="about__fact-value">{fact.value}</dd>
        </div>
      {/each}
    </dl>
  </div>

  <div class="about__body shell">
    <div class="about__prose">
      <p class="about__lede">
        Exceptional Landscaping &amp; Lawn Services is a Family-Owned and Operated business that has been serving the community since 2022,
        we handle regular maintenance, planting, hardscaping, mulching and maintenance.
      </p>

      <p>
      </p>

      <div class="mowline about__rule" use:mowline></div>

      <h2 class="about__h2">What we learned the hard way</h2>

      <p>
      </p>

      <p>
      </p>

      <div class="mowline about__rule" use:mowline></div>

      <h2 class="about__h2">How an estimate works</h2>

      <p>
      </p>

      <p>
      </p>

      <div class="about__cta">
        <a href="/contact" class="btn btn--solid">Request lawn service now</a>
        <a href="/gallery" class="btn">See our work first</a>
      </div>
    </div>

    <aside class="about__aside">
      <div class="about__photo"></div>
      <p class="about__caption">The crew, spring route</p>

      <div class="about__contact">
        <span class="about__contact-label">Call the office</span>
        <a href="tel:+15025550142" class="about__phone">(502) 555-0142</a>
        <p class="about__hours">Monday to Friday, 7am to 5pm</p>
      </div>
    </aside>
  </div>
</article>
