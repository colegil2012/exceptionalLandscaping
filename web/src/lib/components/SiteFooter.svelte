<script>
  import './SiteFooter.css';
  import { env } from '$env/dynamic/public';
  const year = new Date().getFullYear();

  const logo = '/site/logo-transparent.png';
</script>

<footer class="footer">
  <div class="footer__inner shell">
    <div class="footer__brand">
          <img
            class="footer-logo-img"
            src={logo}
            alt="Exceptional Landscaping & Lawn Services"
            loading="lazy"
            decoding="async"
          />
    </div>

    <nav class="footer__col" aria-label="Footer">
      <span class="footer__coltitle">Pages</span>
      <a href="/gallery" class="footer__link">Our Work</a>
      <a href="/bio" class="footer__link">About</a>
      <a href="/contact" class="footer__link">Get a Quote</a>
    </nav>

    <div class="footer__col">
      <span class="footer__coltitle">Service Area</span>
      <span class="footer__item">Louisville</span>
      <span class="footer__item">St. Matthews</span>
      <span class="footer__item">Prospect</span>
      <span class="footer__item">Jeffersontown</span>
    </div>
  </div>

  <div class="footer__base shell">
    <span>&copy; {year} Ells Enterprises</span>
    <a href="/contact" class="footer__cta">Request lawn service</a>
  </div>
</footer>
