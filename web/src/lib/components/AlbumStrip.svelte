<script>
  import { fade } from 'svelte/transition';
  import './AlbumStrip.css';

  /**
   * Horizontally scrollable set of album headers, linking into the gallery.
   * Albums are supplied by the page's load function.
   */
  let { albums = [], limit } = $props();

  const shown = $derived(limit ? albums.slice(0, limit) : albums);
  const status = $derived(shown.length ? 'ready' : 'empty');
</script>

<div class="astrip">
  {#if status === 'ready'}
    <ul class="astrip__track" in:fade={{ duration: 350 }}>
      {#each shown as album (album.id)}
        <li class="astrip__item">
          <a href="/gallery#album-{album.slug}" class="astrip__link">
            <figure class="astrip__figure">
              {#if album.coverUrl}
                <img
                  src={album.coverUrl}
                  alt={album.label}
                  loading="lazy"
                  decoding="async"
                />
              {/if}
              <figcaption class="astrip__caption">
                <span class="astrip__title">{album.label}</span>
                <span class="astrip__meta">
                  {album.count} photo{album.count === 1 ? '' : 's'}
                </span>
              </figcaption>
            </figure>
          </a>
        </li>
      {/each}
    </ul>
  {:else}
    <p class="astrip__notice">No albums published yet.</p>
  {/if}
</div>