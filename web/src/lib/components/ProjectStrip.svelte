<script>
  import { onMount } from 'svelte';
  import { fade } from 'svelte/transition';
  import { api } from '$lib/api/client.js';
  import './ProjectStrip.css';

  /** Horizontally scrollable random selection of completed jobs. */
  let { limit = 8 } = $props();

  let projects = $state([]);
  let status = $state('loading'); // loading | ready | empty | error

  onMount(async () => {
    try {
      const data = await api.gallery({ random: true, limit });
      projects = data ?? [];
      status = projects.length ? 'ready' : 'empty';
    } catch {
      status = 'error';
    }
  });
</script>

<div class="pstrip">
  {#if status === 'loading'}
    <div class="pstrip__track" aria-hidden="true">
      {#each Array(4) as _}
        <div class="pstrip__item pstrip__item--skeleton"></div>
      {/each}
    </div>
  {:else if status === 'ready'}
    <ul class="pstrip__track" in:fade={{ duration: 350 }}>
      {#each projects as project, i (project.id)}
        <li class="pstrip__item">
          <a href="/gallery#{project.id}" class="pstrip__link">
            <figure class="pstrip__figure">
              <img
                src={project.thumbUrl ?? project.imageUrl}
                alt={project.title}
                loading="lazy"
                decoding="async"
                width={project.width}
                height={project.height}
              />
              {#if project.beforeUrl}
                <span class="pstrip__badge">Before / after</span>
              {/if}
              <figcaption class="pstrip__caption">
                <span class="pstrip__title">{project.title}</span>
                <span class="pstrip__meta">
                  {project.location}{project.duration ? ` · ${project.duration}` : ''}
                </span>
              </figcaption>
            </figure>
          </a>
        </li>
      {/each}
    </ul>
  {:else if status === 'empty'}
    <p class="pstrip__notice">No projects published yet.</p>
  {:else}
    <p class="pstrip__notice">Our work could not load. Refresh to try again.</p>
  {/if}
</div>
