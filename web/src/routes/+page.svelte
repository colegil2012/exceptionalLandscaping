<script>
  import { onMount } from 'svelte';
  import ServiceCard from '$components/ServiceCard.svelte';
  import ProjectStrip from '$components/ProjectStrip.svelte';
  import { mowline } from '$scripts/mowline.js';
  import './page.css';

  let mounted = $state(false);
  onMount(() => { mounted = true; });

  const cards = [
    {
      href: '/gallery',
      eyebrow: 'Finished jobs',
      title: 'Our Work',
      blurb: 'Walls, patios, drainage, and lawns we have built across greater Louisville — most with the before shot alongside.',
      cta: 'See the work'
    },
    {
      href: '/bio',
      eyebrow: 'The crew',
      title: 'About Ridgeline',
      blurb: 'Two trucks, four people, and eleven years of learning which slopes hold and which ones do not.',
      cta: 'Meet the crew'
    },
    {
      href: '/contact',
      eyebrow: 'Free estimate',
      title: 'Request Service',
      blurb: 'Weekly mowing through full redesigns. Tell us the address and we will walk it within a week.',
      cta: 'Request lawn service now'
    }
  ];

  const services = [
    { name: 'Mowing & lawn care',   detail: 'Weekly or biweekly, striped and edged every visit' },
    { name: 'Hardscape',            detail: 'Patios, retaining walls, walkways, drainage' },
    { name: 'Planting & beds',      detail: 'Perennials, shrubs, mulch, seasonal color' },
    { name: 'Cleanups',             detail: 'Spring and fall resets, storm debris, pruning' },
    { name: 'Irrigation',           detail: 'Installs, repairs, drip conversions' },
    { name: 'Full redesign',        detail: 'Grading, sod, and everything above, together' }
  ];
</script>

<svelte:head>
  <title>Ridgeline Lawn &amp; Landscape — Louisville, KY</title>
  <meta name="description" content="Lawn care, hardscape, planting, and full landscape redesigns across greater Louisville. Licensed and insured. Free estimates." />
</svelte:head>

<section class="hero" class:hero--in={mounted}>
  <div class="hero__sky">
    <div class="hero__inner shell">
      <span class="eyebrow hero__eyebrow">Louisville, Kentucky · Licensed &amp; insured</span>
      <h1 class="hero__title">
        <span class="hero__line">We cut a</span>
        <span class="hero__line hero__line--accent">clean line.</span>
      </h1>
      <p class="hero__lede">
        Mowing, hardscape, and full redesigns for properties across greater
        Louisville. Eleven years, two trucks, and no subcontractors.
      </p>
      <div class="hero__actions">
        <a href="/contact" class="btn btn--solid">Request lawn service now</a>
        <a href="/gallery" class="btn">See our work</a>
      </div>
    </div>
  </div>

  <!-- The horizon: the hard line between sky and turf, and the site's thesis. -->
  <div class="hero__ground" aria-hidden="true">
    <div class="hero__stripes"></div>
  </div>
</section>

<section class="services">
  <div class="shell">
    <div class="services__head">
      <span class="eyebrow">What we do</span>
      <h2 class="services__title">Six services, one crew</h2>
    </div>
    <div class="mowline services__rule" use:mowline></div>

    <ul class="services__list">
      {#each services as service}
        <li class="services__item">
          <span class="services__name">{service.name}</span>
          <span class="services__detail">{service.detail}</span>
        </li>
      {/each}
    </ul>
  </div>
</section>

<section class="recent">
  <div class="shell">
    <div class="recent__head">
      <span class="eyebrow">A different set each visit</span>
      <h2 class="recent__title">Recent jobs</h2>
    </div>
    <div class="mowline recent__rule" use:mowline></div>
  </div>
  <ProjectStrip limit={8} />
</section>

<section class="highlights shell">
  <div class="highlights__grid">
    {#each cards as card, i}
      <ServiceCard {...card} index={i} />
    {/each}
  </div>
</section>

<section class="closing">
  <div class="closing__inner shell">
    <div>
      <h2 class="closing__title">Booking now for the season</h2>
      <p class="closing__text">
        Most estimates happen within a week of the request. Weekly mowing
        routes fill by early spring.
      </p>
    </div>
    <a href="/contact" class="btn btn--on-turf">Request lawn service now</a>
  </div>
</section>
