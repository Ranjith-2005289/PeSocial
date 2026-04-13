export default function AdminPage() {
  return (
    <div className="mx-auto flex min-h-screen w-full max-w-4xl items-center justify-center p-4 text-white">
      <div className="rounded-xl border border-white/20 bg-white/10 p-6 text-center">
        <h1 className="text-2xl font-semibold">Admin Console</h1>
        <p className="mt-2 text-sm text-white/75">Only users with the `ADMIN` role can access this page.</p>
      </div>
    </div>
  )
}
