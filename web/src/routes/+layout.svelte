<script>
  import '$styles/root.css';
  import './layout.css';
  import SiteNav from '$components/SiteNav.svelte';
  import SiteFooter from '$components/SiteFooter.svelte';

  let { children } = $props();
</script>

<a href="#main" class="skip-link">Skip to content</a>

<SiteNav />

<main id="main">
  {@render children()}
</main>

<SiteFooter />
