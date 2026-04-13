import { useQuery } from '@tanstack/react-query'
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { creatorService } from '../services/creatorService'
import { parseApiError } from '../services/api'
import { useAuth } from '../hooks/useAuth'

export default function AnalyticsPage() {
  const { user } = useAuth()

  const analyticsQuery = useQuery({
    queryKey: ['creator-analytics', user?.id],
    queryFn: () => creatorService.getAnalytics(user!.id),
    enabled: Boolean(user?.id),
  })

  const chartData = analyticsQuery.data
    ? [
        { label: 'Total Likes', value: analyticsQuery.data.totalLikes, color: '#34d399' },
        { label: 'Engagement %', value: Number(analyticsQuery.data.engagementRate.toFixed(2)), color: '#60a5fa' },
      ]
    : []

  return (
    <div className="mx-auto min-h-screen w-full max-w-5xl p-4 text-white">
      <div className="rounded-xl border border-white/20 bg-white/10 p-6">
        <h1 className="text-2xl font-semibold">Creator Analytics Dashboard</h1>
        <p className="mt-1 text-sm text-white/75">Track likes and engagement performance.</p>

        {analyticsQuery.isLoading && <p className="mt-6 text-sm text-white/80">Loading analytics...</p>}

        {analyticsQuery.isError && (
          <p className="mt-6 rounded-lg border border-rose-300/40 bg-rose-500/10 p-3 text-sm text-rose-100">
            {parseApiError(analyticsQuery.error)}
          </p>
        )}

        {analyticsQuery.data && (
          <>
            <div className="mt-6 grid gap-4 md:grid-cols-4">
              <MetricCard title="Total Likes" value={analyticsQuery.data.totalLikes} />
              <MetricCard title="Engagement Rate" value={`${analyticsQuery.data.engagementRate.toFixed(2)}%`} />
              <MetricCard title="Total Views" value={analyticsQuery.data.totalViews} />
              <MetricCard title="Followers" value={analyticsQuery.data.followersCount} />
            </div>

            <div className="mt-6 h-80 rounded-xl border border-white/20 bg-black/20 p-3">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 12, right: 12, bottom: 12, left: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                  <XAxis dataKey="label" tick={{ fill: '#cbd5e1' }} />
                  <YAxis tick={{ fill: '#cbd5e1' }} />
                  <Tooltip
                    contentStyle={{ background: '#0f172a', border: '1px solid #334155', borderRadius: 8 }}
                    labelStyle={{ color: '#e2e8f0' }}
                  />
                  <Bar dataKey="value" radius={[8, 8, 0, 0]}>
                    {chartData.map((entry) => (
                      <Cell key={entry.label} fill={entry.color} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}
      </div>
    </div>
  )
}

function MetricCard({ title, value }: { title: string; value: string | number }) {
  return (
    <div className="rounded-lg border border-white/20 bg-black/20 p-4">
      <p className="text-xs uppercase tracking-wider text-white/70">{title}</p>
      <p className="mt-2 text-2xl font-semibold">{value}</p>
    </div>
  )
}
