import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="mx-auto flex min-h-screen w-full max-w-4xl items-center justify-center p-4 text-white">
      <div className="rounded-xl border border-white/20 bg-white/10 p-6 text-center">
        <h1 className="text-2xl font-semibold">Page not found</h1>
        <p className="mt-2 text-sm text-white/75">The requested route does not exist.</p>
        <Link to="/feed" className="mt-4 inline-block rounded-lg bg-white px-4 py-2 text-sm font-medium text-black">
          Go to Feed
        </Link>
      </div>
    </div>
  )
}
