import { useMemo, useState, type FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useAuth } from '../hooks/useAuth'
import { parseApiError } from '../services/api'

type AuthMode = 'login' | 'register'

export default function LoginPage() {
  const [mode, setMode] = useState<AuthMode>('login')
  const [loading, setLoading] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [username, setUsername] = useState('')
  const [handle, setHandle] = useState('')
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  const { login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const destination = useMemo(() => {
    const state = location.state as { from?: { pathname?: string } } | null
    return state?.from?.pathname || '/feed'
  }, [location.state])

  const title = mode === 'login' ? 'Welcome back' : 'Create your account'
  const subtitle = mode === 'login' ? 'Sign in with your PESocial credentials.' : 'Join PESocial and start sharing.'

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault()
    setLoading(true)
    setErrorMessage(null)

    try {
      if (mode === 'login') {
        await login({ email, password })
        toast.success('Login successful')
      } else {
        await register({ username, handle, email, password })
        toast.success('Registration successful')
      }

      navigate(destination, { replace: true })
    } catch (error) {
      const message = parseApiError(error)
      setErrorMessage(message)
      toast.error(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-screen w-full max-w-5xl items-center justify-center p-4 text-white">
      <div className="grid w-full overflow-hidden rounded-2xl border border-white/20 bg-white/10 shadow-2xl backdrop-blur md:grid-cols-2">
        <div className="hidden flex-col justify-between bg-gradient-to-br from-indigo-600 via-purple-600 to-fuchsia-600 p-8 md:flex">
          <div>
            <p className="text-sm uppercase tracking-widest text-white/80">PESocial</p>
            <h1 className="mt-3 text-3xl font-bold leading-tight">Secure social platform for creators and communities</h1>
          </div>
          <p className="text-sm text-white/90">JWT auth · Role-based routing · Real-time messaging.</p>
        </div>

        <div className="p-6 md:p-8">
          <div className="mb-6 flex rounded-xl bg-black/20 p-1">
            <button
              type="button"
              onClick={() => setMode('login')}
              className={`w-1/2 rounded-lg px-4 py-2 text-sm font-medium transition ${mode === 'login' ? 'bg-white text-black' : 'text-white/80'}`}
            >
              Login
            </button>
            <button
              type="button"
              onClick={() => setMode('register')}
              className={`w-1/2 rounded-lg px-4 py-2 text-sm font-medium transition ${mode === 'register' ? 'bg-white text-black' : 'text-white/80'}`}
            >
              Register
            </button>
          </div>

          <h2 className="text-2xl font-semibold">{title}</h2>
          <p className="mt-1 text-sm text-white/75">{subtitle}</p>

          {errorMessage && (
            <div className="mt-4 rounded-lg border border-rose-300/40 bg-rose-500/10 p-3 text-sm text-rose-100">{errorMessage}</div>
          )}

          <form onSubmit={onSubmit} className="mt-6 space-y-4">
            {mode === 'register' && (
              <>
                <div>
                  <label className="mb-1 block text-sm text-white/85">Display Name</label>
                  <input
                    value={username}
                    onChange={(event) => setUsername(event.target.value)}
                    required
                    className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 outline-none focus:border-fuchsia-400"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-sm text-white/85">Handle</label>
                  <input
                    value={handle}
                    onChange={(event) => setHandle(event.target.value)}
                    required
                    placeholder="@ranjith_k"
                    className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 outline-none focus:border-fuchsia-400"
                  />
                </div>
              </>
            )}

            <div>
              <label className="mb-1 block text-sm text-white/85">Email</label>
              <input
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
                className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 outline-none focus:border-fuchsia-400"
              />
            </div>

            <div>
              <label className="mb-1 block text-sm text-white/85">Password</label>
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                className="w-full rounded-lg border border-white/30 bg-black/20 px-3 py-2 outline-none focus:border-fuchsia-400"
              />
            </div>

            <button
              disabled={loading}
              className="w-full rounded-lg bg-white px-4 py-2 font-semibold text-black transition hover:bg-white/90 disabled:opacity-60"
            >
              {loading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Register'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
