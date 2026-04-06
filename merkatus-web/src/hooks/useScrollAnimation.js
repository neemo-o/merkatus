import { useEffect } from 'react'

export default function useScrollAnimation(selector) {
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const el = entry.target
            const delay = parseFloat(el.dataset.delay || 0)
            setTimeout(() => el.classList.add('visible'), delay)
            observer.unobserve(el)
          }
        })
      },
      { threshold: 0.15, rootMargin: '0px 0px -40px 0px' }
    )
    document.querySelectorAll(selector).forEach((el, i) => {
      el.dataset.delay = el.dataset.delay || `${i * 80}`
      observer.observe(el)
    })
    return () => observer.disconnect()
  }, [selector])
}
