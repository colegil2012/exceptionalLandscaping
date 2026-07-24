<script>
  import { onMount } from 'svelte';
  import { fade, scale } from 'svelte/transition';
  import { api } from '$lib/api/client.js';
  import './page.css';

  let projects = $state([]);
  let status = $state('loading');
  let activeService = $state('all');
  let selected = $state(null);
  let sliderPos = $state(50);

  const services = $derived([
    'all',
    ...new Set(projects.map((p) => p.serviceType).filter(Boolean))
  ]);

  const visible = $derived(
    activeService === 'all'
      ? projects
      : projects.filter((p) => p.serviceType === activeService)
  );

  onMount(async () => {
    try {
      projects = (await api.gallery()) ?? [];
      status = projects.length ? 'ready' : 'empty';
    } catch {
      status = 'error';
    }
  });

  function open(project) {
    selected = project;
    sliderPos = 50;
  }

  function onKeydown(event) {
    if (event.key === 'Escape') selected = null;
  }

  /** Turns a service key like "full-redesign" into "Full redesign". */
  function label(key) {
    if (key === 'all') return 'All work';
    const spaced = key.replace(/-/g, ' ');
    return spaced.charAt(0).toUpperCase() + spaced.slice(1);
  }
</script>

<svelte:head>
  <title>Our Work — Ridgeline Lawn &amp; Landscape</title>
  <meta name="description" content="Completed landscaping, hardscape, and lawn care projects across greater Louisville." />
</svelte:head>

<svelte:window on:keydown={onKeydown} />

<header class="work-head shell">
  <span class="eyebrow">Finished jobs</span>
  <h1 class="work-head__title">Our Work</h1>
  <p class="work-head__lede">
    Every project here is one we built ourselves. Where we have the before
    shot, you can drag to compare.
  </p>

  {#if status === 'ready'}
    <div class="filters" role="group" aria-label="Filter by service">
      {#each services as service}
        <button
          class="filters__tag"
          class:filters__tag--on={activeService === service}
          onclick={() => (activeService = service)}
        >
          {label(service)}
        </button>
      {/each}
    </div>
  {/if}
</header>

<div class="work shell">
  {#if status === 'loading'}
    <div class="work__grid">
      {#each Array(6) as _}
        <div class="work__skeleton"></div>
      {/each}
    </div>
  {:else if status === 'ready'}
    <div class="work__grid">
      {#each visible as project (project.id)}
        <button
          class="jobcard"
          id={project.id}
          onclick={() => open(project)}
          in:fade={{ duration: 260 }}
        >
          <span class="jobcard__media">
            <img
              src={project.thumbUrl ?? project.imageUrl}
              alt={project.title}
              loading="lazy"
              decoding="async"
              width={project.width}
              height={project.height}
            />
            {#if project.beforeUrl}
              <span class="jobcard__badge">Before / after</span>
            {/if}
          </span>
          <span class="jobcard__body">
            <span class="jobcard__title">{project.title}</span>
            <span class="jobcard__meta">
              {project.location}{project.duration ? ` · ${project.duration}` : ''}
            </span>
          </span>
        </button>
      {/each}
    </div>
  {:else if status === 'empty'}
    <p class="work__notice">No projects published yet. Check back soon.</p>
  {:else}
    <p class="work__notice">Our work could not load. Refresh to try again.</p>
  {/if}
</div>

{#if selected}
  <div
    class="lightbox"
    role="dialog"
    aria-modal="true"
    aria-label={selected.title}
    transition:fade={{ duration: 200 }}
  >
    <button class="lightbox__scrim" onclick={() => (selected = null)} aria-label="Close"></button>

    <div class="lightbox__panel" transition:scale={{ duration: 280, start: 0.97 }}>
      <div class="lightbox__media">
        {#if selected.beforeUrl}
          <!-- Before/after comparison. The range input is the real control;
               it sits on top invisibly so keyboard and touch both work. -->
          <div class="compare" style="--pos: {sliderPos}%">
            <img class="compare__after" src={selected.imageUrl} alt="{selected.title}, finished" />
            <div class="compare__before-wrap">
              <img class="compare__before" src={selected.beforeUrl} alt="{selected.title}, before" />
            </div>
            <div class="compare__handle" aria-hidden="true">
              <span class="compare__grip"></span>
            </div>
            <input
              class="compare__range"
              type="range"
              min="0"
              max="100"
              bind:value={sliderPos}
              aria-label="Compare before and after"
            />
            <span class="compare__tag compare__tag--before">Before</span>
            <span class="compare__tag compare__tag--after">After</span>
          </div>
        {:else}
          <img class="lightbox__single" src={selected.imageUrl ?? selected.thumbUrl} alt={selected.title} />
        {/if}
      </div>

      <div class="lightbox__info">
        <span class="eyebrow">{label(selected.serviceType ?? '')}</span>
        <h2 class="lightbox__title">{selected.title}</h2>
        <dl class="lightbox__facts">
          {#if selected.location}
            <div class="lightbox__fact">
              <dt>Location</dt>
              <dd>{selected.location}</dd>
            </div>
          {/if}
          {#if selected.duration}
            <div class="lightbox__fact">
              <dt>Duration</dt>
              <dd>{selected.duration}</dd>
            </div>
          {/if}
        </dl>
        {#if selected.description}
          <p class="lightbox__desc">{selected.description}</p>
        {/if}
        <a href="/contact" class="btn btn--solid lightbox__cta">Request something like this</a>
      </div>

      <button class="lightbox__close" onclick={() => (selected = null)}>
        <span class="visually-hidden">Close</span>
        <svg viewBox="0 0 20 20" aria-hidden="true">
          <path d="M4 4l12 12M16 4L4 16" stroke="currentColor" stroke-width="2" fill="none" />
        </svg>
      </button>
    </div>
  </div>
{/if}
