import React, { useState, useMemo } from 'react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, AreaChart, Area
} from 'recharts';
import { 
  TrendingUp, 
  MapPin, 
  Clock, 
  DollarSign, 
  Plus, 
  Trophy,
  History,
  LayoutDashboard,
  Settings,
  Car
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { RideData, RideCategory, ScoreRating } from './types';
import { analyzeRide } from './lib/rideAnalyzer';

export default function App() {
  const [rides, setRides] = useState<RideData[]>([]);
  const [form, setForm] = useState({
    price: '',
    distance: '',
    time: '',
    category: RideCategory.X
  });

  const analyzedRides = useMemo(() => {
    return rides.map(ride => ({
      ...ride,
      analysis: analyzeRide(ride)
    }));
  }, [rides]);

  const stats = useMemo(() => {
    if (rides.length === 0) return { totalEarnings: 0, avgScore: 0, totalKm: 0 };
    const totalEarnings = rides.reduce((acc, r) => acc + r.price, 0);
    const totalKm = rides.reduce((acc, r) => acc + r.distanceKm, 0);
    const avgScore = analyzedRides.reduce((acc, r) => acc + r.analysis.score, 0) / rides.length;
    return { totalEarnings, avgScore, totalKm };
  }, [rides, analyzedRides]);

  const handleAddRide = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.price || !form.distance || !form.time) return;

    const newRide: RideData = {
      id: Math.random().toString(36).substr(2, 9),
      price: parseFloat(form.price),
      distanceKm: parseFloat(form.distance),
      timeMin: parseInt(form.time),
      category: form.category,
      timestamp: new Date()
    };

    setRides([newRide, ...rides]);
    setForm({ price: '', distance: '', time: '', category: RideCategory.X });
  };

  return (
    <div className="min-h-screen bg-[#0A0A0B] text-gray-100 font-sans selection:bg-orange-500/30">
      {/* Sidebar - Desktop */}
      <aside className="fixed left-0 top-0 h-full w-64 bg-[#121214] border-r border-white/5 hidden lg:flex flex-col">
        <div className="p-6 flex items-center gap-3">
          <div className="w-10 h-10 bg-orange-500 rounded-xl flex items-center justify-center shadow-lg shadow-orange-500/20">
            <Car className="text-white w-6 h-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg tracking-tight">Uber Analyzer</h1>
            <p className="text-[10px] text-gray-500 uppercase tracking-widest font-semibold">Pro Driver Tools</p>
          </div>
        </div>

        <nav className="flex-1 px-4 mt-4 space-y-1">
          <button className="w-full flex items-center gap-3 px-4 py-3 bg-white/5 text-white rounded-xl font-medium transition-all border border-white/5">
            <LayoutDashboard className="w-5 h-5 text-orange-500" />
            Dashboard
          </button>
          <button className="w-full flex items-center gap-3 px-4 py-3 text-gray-400 hover:text-white hover:bg-white/5 rounded-xl font-medium transition-all group">
            <History className="w-5 h-5 group-hover:text-orange-500" />
            Histórico
          </button>
          <button className="w-full flex items-center gap-3 px-4 py-3 text-gray-400 hover:text-white hover:bg-white/5 rounded-xl font-medium transition-all group">
            <Settings className="w-5 h-5 group-hover:text-orange-500" />
            Ajustes
          </button>
        </nav>

        <div className="p-6 mt-auto">
          <div className="bg-orange-500/10 border border-orange-500/20 rounded-2xl p-4">
            <p className="text-xs text-orange-200/60 mb-2">Versão Profissional Ativa</p>
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
              <span className="text-sm font-medium">Análise em Tempo Real</span>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="lg:ml-64 p-4 lg:p-8 space-y-8 pb-24 lg:pb-8">
        {/* Header Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-[#121214] p-6 rounded-3xl border border-white/5 relative overflow-hidden group"
          >
            <div className="absolute -right-4 -top-4 w-24 h-24 bg-orange-500/5 rounded-full blur-2xl group-hover:bg-orange-500/10 transition-colors" />
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-gray-400 font-medium">Ganhos Totais</p>
              <div className="w-10 h-10 bg-green-500/10 rounded-xl flex items-center justify-center">
                <DollarSign className="w-5 h-5 text-green-500" />
              </div>
            </div>
            <p className="text-3xl font-bold tracking-tight">R$ {stats.totalEarnings.toFixed(2)}</p>
            <div className="flex items-center gap-2 mt-2 text-xs text-green-500">
              <TrendingUp className="w-3 h-3" />
              <span>+12.5% vs semana anterior</span>
            </div>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="bg-[#121214] p-6 rounded-3xl border border-white/5 relative overflow-hidden group"
          >
            <div className="absolute -right-4 -top-4 w-24 h-24 bg-blue-500/5 rounded-full blur-2xl group-hover:bg-blue-500/10 transition-colors" />
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-gray-400 font-medium">Score Médio</p>
              <div className="w-10 h-10 bg-orange-500/10 rounded-xl flex items-center justify-center">
                <Trophy className="w-5 h-5 text-orange-500" />
              </div>
            </div>
            <p className="text-3xl font-bold tracking-tight">{stats.avgScore.toFixed(1)} <span className="text-lg text-gray-500 font-medium">/ 10</span></p>
            <p className="text-xs text-gray-500 mt-2">Baseado em {rides.length} corridas</p>
          </motion.div>

          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="bg-[#121214] p-6 rounded-3xl border border-white/5 relative overflow-hidden group"
          >
            <div className="absolute -right-4 -top-4 w-24 h-24 bg-purple-500/5 rounded-full blur-2xl group-hover:bg-purple-500/10 transition-colors" />
            <div className="flex items-center justify-between mb-4">
              <p className="text-sm text-gray-400 font-medium">Rodagem Total</p>
              <div className="w-10 h-10 bg-purple-500/10 rounded-xl flex items-center justify-center">
                <MapPin className="w-5 h-5 text-purple-500" />
              </div>
            </div>
            <p className="text-3xl font-bold tracking-tight">{stats.totalKm.toFixed(1)} km</p>
            <p className="text-xs text-gray-500 mt-2">Média de {(stats.totalKm / (rides.length || 1)).toFixed(1)} km/corrida</p>
          </motion.div>
        </div>

        <div className="grid grid-cols-1 xl:grid-cols-3 gap-8">
          {/* Main Chart Area */}
          <div className="xl:col-span-2 space-y-8">
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
              className="bg-[#121214] p-6 rounded-3xl border border-white/5"
            >
              <div className="flex items-center justify-between mb-8">
                <div>
                  <h3 className="text-lg font-bold">Performance de Lucro</h3>
                  <p className="text-sm text-gray-500">Valor por KM ao longo do tempo</p>
                </div>
                <div className="flex gap-2">
                  <span className="flex items-center gap-2 text-xs px-3 py-1 bg-white/5 rounded-full">
                    <div className="w-2 h-2 bg-orange-500 rounded-full" /> Lucro/km
                  </span>
                </div>
              </div>
              
              <div className="h-[300px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={analyzedRides.slice().reverse()}>
                    <defs>
                      <linearGradient id="colorProfit" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#f97316" stopOpacity={0.3}/>
                        <stop offset="95%" stopColor="#f97316" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#ffffff0a" vertical={false} />
                    <XAxis 
                      dataKey="timestamp" 
                      hide
                    />
                    <YAxis stroke="#ffffff4D" fontSize={10} axisLine={false} tickLine={false} />
                    <Tooltip 
                      contentStyle={{ backgroundColor: '#18181b', border: '1px solid #ffffff14', borderRadius: '12px' }}
                      itemStyle={{ color: '#fff' }}
                    />
                    <Area type="monotone" dataKey="analysis.pricePerKm" stroke="#f97316" fillOpacity={1} fill="url(#colorProfit)" strokeWidth={3} />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </motion.div>

            {/* List of Rides */}
            <div className="space-y-4">
              <h3 className="text-lg font-bold flex items-center gap-2 px-2">
                <History className="w-5 h-5 text-orange-500" />
                Corridas Recentes
              </h3>
              <div className="space-y-3">
                <AnimatePresence mode="popLayout">
                  {analyzedRides.length === 0 ? (
                    <motion.div 
                      initial={{ opacity: 0 }}
                      animate={{ opacity: 1 }}
                      className="text-center py-20 bg-[#121214] rounded-3xl border border-dashed border-white/10 text-gray-500"
                    >
                      Nenhuma corrida registrada ainda.
                    </motion.div>
                  ) : (
                    analyzedRides.map((ride) => (
                      <motion.div 
                        key={ride.id}
                        layout
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        exit={{ opacity: 0, scale: 0.95 }}
                        className="bg-[#121214] p-4 rounded-2xl border border-white/5 flex items-center gap-4 hover:border-white/10 transition-colors"
                      >
                        <div className={`w-12 h-12 rounded-xl flex items-center justify-center shrink-0 ${
                          ride.analysis.score >= 7 ? 'bg-green-500/10 text-green-500' :
                          ride.analysis.score >= 4 ? 'bg-orange-500/10 text-orange-500' : 'bg-red-500/10 text-red-500'
                        }`}>
                          <span className="font-bold text-lg">{ride.analysis.score.toFixed(1)}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <span className="font-bold">R$ {ride.price.toFixed(2)}</span>
                            <span className="text-[10px] uppercase font-bold px-2 py-0.5 bg-white/5 rounded text-gray-400">
                              {ride.category}
                            </span>
                          </div>
                          <div className="flex items-center gap-3 text-xs text-gray-500 mt-1">
                            <span className="flex items-center gap-1"><MapPin className="w-3 h-3" /> {ride.distanceKm}km</span>
                            <span className="flex items-center gap-1"><Clock className="w-3 h-3" /> {ride.timeMin}m</span>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-xs font-medium text-gray-400">{ride.analysis.rating}</p>
                          <p className="text-[10px] text-gray-600 mt-1">
                            {format(ride.timestamp, 'HH:mm', { locale: ptBR })}
                          </p>
                        </div>
                      </motion.div>
                    ))
                  )}
                </AnimatePresence>
              </div>
            </div>
          </div>

          {/* Right Panel - Add New Ride */}
          <div className="space-y-6">
            <div className="bg-[#121214] p-6 rounded-3xl border border-white/5 sticky top-8">
              <div className="flex items-center gap-3 mb-6">
                <div className="w-10 h-10 bg-orange-500/10 rounded-xl flex items-center justify-center text-orange-500">
                  <Plus className="w-6 h-6" />
                </div>
                <h3 className="font-bold text-lg">Nova Corrida</h3>
              </div>

              <form onSubmit={handleAddRide} className="space-y-4">
                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold text-gray-500 uppercase tracking-widest px-1">Ganhos (R$)</label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                    <input 
                      type="number"
                      step="0.01"
                      className="w-full bg-white/5 border border-white/5 rounded-xl py-3 pl-10 pr-4 focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/50 outline-none transition-all placeholder:text-gray-600"
                      placeholder="0.00"
                      value={form.price}
                      onChange={e => setForm({...form, price: e.target.value})}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1.5">
                    <label className="text-[10px] font-bold text-gray-500 uppercase tracking-widest px-1">Distância (KM)</label>
                    <div className="relative">
                      <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                      <input 
                        type="number"
                        step="0.1"
                        className="w-full bg-white/5 border border-white/5 rounded-xl py-3 pl-10 pr-4 focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/50 outline-none transition-all"
                        placeholder="0.0"
                        value={form.distance}
                        onChange={e => setForm({...form, distance: e.target.value})}
                      />
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-[10px] font-bold text-gray-500 uppercase tracking-widest px-1">Tempo (MIN)</label>
                    <div className="relative">
                      <Clock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                      <input 
                        type="number"
                        className="w-full bg-white/5 border border-white/5 rounded-xl py-3 pl-10 pr-4 focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/50 outline-none transition-all"
                        placeholder="0"
                        value={form.time}
                        onChange={e => setForm({...form, time: e.target.value})}
                      />
                    </div>
                  </div>
                </div>

                <div className="space-y-1.5">
                  <label className="text-[10px] font-bold text-gray-500 uppercase tracking-widest px-1">Categoria Uber</label>
                  <div className="grid grid-cols-2 gap-2">
                    {Object.values(RideCategory).map((cat) => (
                      <button
                        key={cat}
                        type="button"
                        onClick={() => setForm({...form, category: cat})}
                        className={`py-2 px-3 rounded-xl text-xs font-bold transition-all border ${
                          form.category === cat 
                            ? 'bg-orange-500 text-white border-orange-500 shadow-lg shadow-orange-500/20' 
                            : 'bg-white/5 text-gray-400 border-white/5 hover:border-white/20'
                        }`}
                      >
                        {cat}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="pt-4">
                  <button 
                    type="submit"
                    className="w-full bg-white text-black font-bold py-4 rounded-2xl hover:bg-orange-500 hover:text-white transition-all transform active:scale-[0.98] shadow-lg"
                  >
                    Analisar Corrida
                  </button>
                </div>
              </form>

              {/* Quick Insight */}
              {form.price && form.distance && (
                <div className="mt-6 p-4 bg-orange-500/10 border border-orange-500/20 rounded-2xl animate-in fade-in slide-in-from-top-4">
                  <p className="text-xs text-orange-200/60 mb-1">Previsão de Lucro</p>
                  <p className="text-xl font-bold">R$ {(parseFloat(form.price) / (parseFloat(form.distance) || 1)).toFixed(2)} <span className="text-xs font-medium text-gray-500">/ km</span></p>
                </div>
              )}
            </div>
            
            <div className="p-6 bg-[#121214] rounded-3xl border border-white/5">
              <h4 className="font-bold text-sm mb-4">Dicas do Especialista</h4>
              <ul className="space-y-3 text-xs text-gray-400">
                <li className="flex gap-2">
                  <div className="w-1.5 h-1.5 bg-orange-500 rounded-full shrink-0 mt-1" />
                  Corridas abaixo de R$ 1.80/km costumam não ser lucrativas.
                </li>
                <li className="flex gap-2">
                  <div className="w-1.5 h-1.5 bg-orange-500 rounded-full shrink-0 mt-1" />
                  No Comfort, foque em Score acima de 6.0 para máxima eficiência.
                </li>
              </ul>
            </div>
          </div>
        </div>
      </main>

      {/* Mobile Nav */}
      <div className="lg:hidden fixed bottom-0 left-0 right-0 h-16 bg-[#121214] border-t border-white/5 px-6 flex items-center justify-around z-50">
        <button className="text-orange-500"><LayoutDashboard className="w-6 h-6" /></button>
        <button className="text-gray-500"><History className="w-6 h-6" /></button>
        <button className="text-gray-500"><Plus className="w-6 h-6" /></button>
        <button className="text-gray-500"><Settings className="w-6 h-6" /></button>
      </div>
    </div>
  );
}

