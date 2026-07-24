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
  <title>About — Ridgeline Lawn &amp; Landscape</title>
  <meta name="description" content="Ridgeline is a four-person landscaping crew working across greater Louisville since 2015." />
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
        Ridgeline started with one truck, a used mower, and a route of eleven
        yards in Crescent Hill. The route is bigger now. The approach has not
        changed much.
      </p>

      <p>
        We do the work ourselves. No subcontractors, no rotating crews you have
        not met. The four people who show up at your property are the same four
        every visit, which matters more than it sounds like it should — they
        learn where the wet spot is, which downspout backs up, and which side
        of the house the dog uses.
      </p>

      <div class="mowline about__rule" use:mowline></div>

      <h2 class="about__h2">What we learned the hard way</h2>

      <p>
        Most of what goes wrong in this trade is water. A patio that heaves, a
        wall that leans, a lawn that will not fill in — trace it back far
        enough and it is almost always drainage that was never solved, only
        covered up.
      </p>

      <p>
        So we grade first and plant second. It makes some estimates higher than
        the competing bid, and we lose jobs over it. The ones we win tend to
        still look right in five years.
      </p>

      <div class="mowline about__rule" use:mowline></div>

      <h2 class="about__h2">How an estimate works</h2>

      <p>
        Send the address and a short description. We walk the property, usually
        within a week, and take our own measurements — a quote written from
        photos is a guess.
      </p>

      <p>
        You get a written scope and a fixed price, not a range. If we hit
        something underground that nobody could have known about, we stop and
        call before spending your money.
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
