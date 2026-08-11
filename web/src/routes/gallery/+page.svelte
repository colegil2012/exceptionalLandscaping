<script>
  import { onMount } from 'svelte';
  import { fade, scale } from 'svelte/transition';
  import './page.css';

  let { data } = $props();

  // Server-loaded collections.
  const albums = $derived(data.albums ?? []);
  const categories = $derived(data.categories ?? []);
  const projects = $derived(data.projects ?? []);

  // Status is derived from what the load gave us.
  const status = $derived(
    data.loadError ? 'error' : projects.length ? 'ready' : 'empty'
  );

  let openAlbum = $state(null);   // album object, or null for the index view
  let activeService = $state('all');
  let selected = $state(null);
  let sliderPos = $state(50);

  // A synthetic album collecting every image, used by "View all work".
  const allWorkAlbum = $derived({ id: '__all', label: 'All work', images: projects, coverImageId: null });

  // Images to render in the grid: the open album's images, or everything.
  const inAlbum = $derived(openAlbum ? openAlbum.images : projects);

  // Category chips reflect whatever's in scope: the open album, or the whole
  // gallery when we're on the album index.
  const services = $derived([
    'all',
    ...new Set((openAlbum ? inAlbum : projects).flatMap((p) => p.serviceTypes).filter(Boolean))
  ]);

  // Album index, filtered by the active category: keep albums where any image
  // carries that category tag.
  const indexAlbums = $derived(
    activeService === 'all'
      ? albums
      : albums.filter((a) => a.images.some((p) => p.serviceTypes.includes(activeService)))
  );

  const visible = $derived(
    activeService === 'all'
      ? inAlbum
      : inAlbum.filter((p) => p.serviceTypes.includes(activeService))
  );

  // Data is present at first paint now; just resolve any deep-link on mount.
  onMount(() => {
    applyHash();
  });

  /** Resolve #imageId (or #album-slug) into the right album + open state. */
  function applyHash() {
    const raw = location.hash.slice(1);
    if (!raw) return;

    // An album deep-link, e.g. #album-backyard-reset
    if (raw.startsWith('album-')) {
      const slug = raw.slice('album-'.length);
      const album = albums.find((a) => a.slug === slug);
      if (album) openAlbum = album;
      return;
    }

    // Otherwise treat it as an image id: open its album and pop the lightbox.
    const project = projects.find((p) => p.id === raw);
    if (!project) return;
    const home = albums.find((a) => a.images.some((p) => p.id === project.id)) ?? allWorkAlbum;
    openAlbum = home;
    open(project);
  }

  function enterAlbum(album) {
    openAlbum = album;
    // Reflect in the URL so the view is shareable and back-button friendly.
    if (album && album.slug) {
      history.replaceState(null, '', `#album-${album.slug}`);
    } else {
      history.replaceState(null, '', location.pathname + location.search);
    }
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function backToIndex() {
    openAlbum = null;
    activeService = 'all';
    history.replaceState(null, '', location.pathname + location.search);
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
  <title>Our Work | Exceptional Landscaping &amp; Lawn Services</title>
  <meta name="description" content="Completed landscaping, hardscape, and lawn care projects across greater Louisville." />
</svelte:head>

<svelte:window on:keydown={onKeydown} on:hashchange={applyHash} />

<header class="work-head shell">
  {#if openAlbum}
    <button class="work-head__back" onclick={backToIndex}>← All albums</button>
  {/if}
  <span class="eyebrow">Finished jobs</span>
  {#if openAlbum}
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
    {#if indexAlbums.length === 0}
      <p class="work__notice">No albums match that service.</p>
    {:else}
      <div class="albums__grid">
        {#each indexAlbums as album (album.id)}
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

        <button class="albumcard albumcard--all" onclick={() => enterAlbum(allWorkAlbum)}>
          <span class="albumcard__body">
            <span class="albumcard__title">View all work</span>
            <span class="albumcard__meta">{projects.length} photos</span>
          </span>
        </button>
      </div>
    {/if}

  {:else}
    <!-- Album detail (or flat grid when there are no albums). -->
    <div class="work__grid" class:work--album={openAlbum}>
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