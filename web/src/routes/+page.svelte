<script>
  import { onMount } from 'svelte';
  import { fade, scale } from 'svelte/transition';
  import { api } from '$lib/api/client.js';
  import './page.css';

  let albums = $state([]);
  let categories = $state([]);
  let projects = $state([]);
  let status = $state('loading');

  let openAlbum = $state(null);   // album object, or null for the index view
  let activeService = $state('all');
  let selected = $state(null);
  let sliderPos = $state(50);

  // Images to render in the grid: the open album's images, or everything.
  const inAlbum = $derived(openAlbum ? openAlbum.images : projects);

  // Every category present anywhere — the chip row on both index and detail.
  const services = $derived([
    'all',
    ...new Set(
      (openAlbum ? inAlbum : projects).flatMap((p) => p.serviceTypes).filter(Boolean)
    )
  ]);

  // Album index, filtered to albums that contain any image in the active category.
  const visibleAlbums = $derived(
    activeService === 'all'
      ? albums
      : albums.filter((a) => a.images.some((p) => p.serviceTypes.includes(activeService)))
  );

  const visible = $derived(
    activeService === 'all'
      ? inAlbum
      : inAlbum.filter((p) => p.serviceTypes.includes(activeService))
  );

  onMount(async () => {
    try {
      const data = await api.galleryGrouped();
      albums = data.albums ?? [];
      categories = data.categories ?? [];
      projects = data.projects ?? [];
      status = projects.length ? 'ready' : 'empty';
      openFromHash();
    } catch {
      status = 'error';
    }
  });

  /** Which album should hold a given image? Its first album, else "All work". */
  function albumForImage(project) {
    if (!project) return null;
    const owning = albums.find((a) => a.id === project.albumIds[0]);
    return owning ?? { label: 'All work', images: projects, coverImageId: null };
  }

  /** /gallery#<imageId> → drill into the owning album and open the lightbox. */
  function openFromHash() {
    const id = decodeURIComponent(location.hash.slice(1));
    if (!id) return;
    const project = projects.find((p) => p.id === id);
    if (!project) return;
    openAlbum = albumForImage(project);
    activeService = 'all';
    open(project);
  }

  function enterAlbum(album) {
    openAlbum = album;
    activeService = 'all';
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function backToIndex() {
    openAlbum = null;
    activeService = 'all';
  }

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
    const spaced = (key ?? '').replace(/-/g, ' ');
    return spaced.charAt(0).toUpperCase() + spaced.slice(1);
  }
</script>

<svelte:head>
  <title>Our Work — Exceptional Landscaping &amp; Lawn Services</title>
  <meta name="description" content="Completed landscaping, hardscape, and lawn care projects across greater Louisville." />
</svelte:head>

<svelte:window on:keydown={onKeydown} on:hashchange={openFromHash} />

<header class="work-head shell">
  <span class="eyebrow">Finished jobs</span>
  {#if openAlbum}
    <button class="work-head__back" onclick={backToIndex}>← All albums</button>
    <h1 class="work-head__title">{openAlbum.label}</h1>
  {:else}
    <h1 class="work-head__title">Our Work</h1>
    <p class="work-head__lede">
      Browse by job below, or open an album to see every shot from that project.
    </p>
  {/if}

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

  {:else if status === 'empty'}
    <p class="work__notice">No projects published yet. Check back soon.</p>

  {:else if status === 'error'}
    <p class="work__notice">Our work could not load. Refresh to try again.</p>

  {:else if !openAlbum && albums.length}
    <!-- Album index: one card per job, plus a "view everything" tile. -->
    <div class="albums__grid">
      {#each visibleAlbums as album (album.id)}
        <button class="albumcard" onclick={() => enterAlbum(album)} in:fade={{ duration: 260 }}>
          <span class="albumcard__media">
            {#if album.images[0]}
              <img
                src={(album.images.find((p) => p.id === album.coverImageId) ?? album.images[0]).thumbUrl
                     ?? (album.images.find((p) => p.id === album.coverImageId) ?? album.images[0]).imageUrl}
                alt={album.label}
                loading="lazy"
                decoding="async"
              />
            {/if}
            <span class="albumcard__count">{album.images.length} photo{album.images.length === 1 ? '' : 's'}</span>
          </span>
          <span class="albumcard__body">
            <span class="albumcard__title">{album.label}</span>
          </span>
        </button>
      {/each}

      {#if activeService === 'all'}
        <button class="albumcard albumcard--all" onclick={() => enterAlbum({ label: 'All work', images: projects, coverImageId: null })}>
          <span class="albumcard__body">
            <span class="albumcard__title">View all work</span>
            <span class="albumcard__meta">{projects.length} photos</span>
          </span>
        </button>
      {/if}
    </div>

  {:else}
    <!-- Album detail (or flat grid when there are no albums). -->
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
              {project.serviceLabel ?? ''}{project.location ? ` · ${project.location}` : ''}
            </span>
          </span>
        </button>
      {/each}
    </div>
  {/if}
</div>