import { useEffect, useState, useMemo, useRef } from 'react'
import { supabase } from './supabaseClient'
import { jsPDF } from 'jspdf'
import 'jspdf-autotable'
import { 
  LayoutDashboard, 
  Users, 
  FileText, 
  Settings, 
  Search, 
  Bell,
  Trash2,
  FileSearch,
  CheckCircle2,
  Clock,
  RefreshCw,
  ChevronDown,
  MapPin,
  TrendingUp,
  AlertCircle,
  Save,
  LogOut,
  X,
  User,
  Lock,
  Mail,
  Download,
  Sun,
  Moon
} from 'lucide-react'

// Toast Component
function Toast({ message, type, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000)
    return () => clearTimeout(timer)
  }, [onClose])

  return (
    <div className={`toast ${type === 'success' ? 'toast-success' : 'toast-error'}`}>
      <div className="flex items-center gap-3">
        {type === 'success' ? (
          <CheckCircle2 className="w-5 h-5" />
        ) : (
          <AlertCircle className="w-5 h-5" />
        )}
        <span>{message}</span>
      </div>
      <button onClick={onClose} className="ml-4 hover:opacity-70">
        <X className="w-4 h-4" />
      </button>
    </div>
  )
}

// Login Page Component - CORRIGÉ
function LoginPage({ onLogin, isLoading }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [localLoading, setLocalLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    if (!email || !password) {
      setError('Veuillez remplir tous les champs')
      return
    }

    setLocalLoading(true)
    try {
      // On attend la réponse de la fonction de login
      const result = await onLogin(email, password)
      
      // Si on reçoit une erreur, on l'affiche
      if (result && result.error) {
        setError(result.error)
      }
    } catch (err) {
      setError('Une erreur inattendue est survenue')
    } finally {
      setLocalLoading(false)
    }
  }

  const showLoading = isLoading || localLoading

  return (
    <div className="min-h-screen bg-linear-to-br from-primary-500 via-primary-600 to-primary-700 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Login Card */}
        <div className="bg-white rounded-3xl shadow-2xl p-8 transition-all">
          {/* Logo */}
          <div className="text-center mb-8">
            <div className="w-20 h-20 bg-linear-to-br from-primary-500 to-primary-600 rounded-2xl flex items-center justify-center mx-auto shadow-lg shadow-primary-500/30 mb-4">
              <FileSearch className="w-10 h-10 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-gray-900">PertePiece</h1>
            <p className="text-gray-500 mt-1">Administration</p>
          </div>

          {/* Error Popup - Visible et Stylisé (Animation retirée pour garantir la visibilité) */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3 shadow-sm" role="alert">
              <div className="shrink-0 mt-0.5">
                <AlertCircle className="w-5 h-5 text-red-600" />
              </div>
              <div className="flex-1">
                <p className="text-sm font-semibold text-red-800">Erreur de connexion</p>
                <p className="text-sm text-red-600 mt-1">{error}</p>
              </div>
              <button 
                onClick={() => setError('')}
                className="shrink-0 text-red-400 hover:text-red-600 transition-colors"
                aria-label="Fermer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          )}

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Adresse email
              </label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={`w-full pl-12 pr-4 py-3 bg-gray-50 border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-400 focus:bg-white transition-all ${error ? 'border-red-300 bg-red-50/30' : 'border-gray-200'}`}
                  placeholder="admin@pertepiece.com"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Mot de passe
              </label>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={`w-full pl-12 pr-4 py-3 bg-gray-50 border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-400 focus:bg-white transition-all ${error ? 'border-red-300 bg-red-50/30' : 'border-gray-200'}`}
                  placeholder="••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={showLoading}
              className="w-full py-3 bg-linear-to-r from-primary-500 to-primary-600 hover:from-primary-600 hover:to-primary-700 text-white rounded-xl font-semibold text-sm transition-all shadow-lg shadow-primary-500/30 hover:shadow-xl hover:shadow-primary-500/40 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {showLoading ? (
                <>
                  <RefreshCw className="w-5 h-5 animate-spin" />
                  Connexion...
                </>
              ) : (
                'Se connecter'
              )}
            </button>
          </form>

          {/* Footer */}
          <p className="text-center text-xs text-gray-400 mt-8">
            Accès réservé aux administrateurs
          </p>
        </div>
      </div>
    </div>
  )
}

function App() {
  // Auth state
  const [session, setSession] = useState(null)
  const [authLoading, setAuthLoading] = useState(true)
  
  const [declarations, setDeclarations] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [activeTab, setActiveTab] = useState('dashboard')
  const [toast, setToast] = useState({ show: false, message: '', type: 'success' })
  
  // Notification dropdown state
  const [showNotifications, setShowNotifications] = useState(false)
  const notificationRef = useRef(null)
  
  // Avatar dropdown state
  const [showAvatarMenu, setShowAvatarMenu] = useState(false)
  const avatarRef = useRef(null)
  
  // Settings state
  const [adminName, setAdminName] = useState('Admin')
  const [notificationEmail, setNotificationEmail] = useState('admin@pertepiece.com')
  
  // Dark mode state with localStorage persistence
  const [darkMode, setDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('darkMode') === 'true'
    }
    return false
  })
  
  // Apply dark mode class to html element
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
    localStorage.setItem('darkMode', darkMode.toString())
  }, [darkMode])

  // Toggle dark mode
  const toggleDarkMode = () => {
    setDarkMode(prev => !prev)
  }

  // Close dropdowns when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotifications(false)
      }
      if (avatarRef.current && !avatarRef.current.contains(event.target)) {
        setShowAvatarMenu(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  // Check auth session on mount
  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session)
      setAuthLoading(false)
    })

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session)
    })

    return () => subscription.unsubscribe()
  }, [])

  // Show toast helper
  const showToast = (message, type = 'success') => {
    setToast({ show: true, message, type })
  }

  // Login handler - CORRIGÉ pour renvoyer l'erreur explicitement
  const handleLogin = async (email, password) => {
    // setAuthLoading(true) removed to prevent unmounting
    try {
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password
      })
      
      if (error) {
        console.error('Login error:', error)
        let errorMessage = 'Une erreur est survenue lors de la connexion'
        
        // Handle different Supabase error types
        if (error.message === 'Invalid login credentials' || 
            error.message.includes('Invalid login credentials') ||
            error.status === 400) {
          errorMessage = 'Email ou mot de passe incorrect'
        } else if (error.message.includes('Email not confirmed')) {
          errorMessage = 'Veuillez confirmer votre email avant de vous connecter'
        } else if (error.message.includes('Network')) {
          errorMessage = 'Erreur réseau. Vérifiez votre connexion.'
        }
        
        // setAuthLoading(false) removed
        return { error: errorMessage } // Retourne l'erreur à LoginPage
      }
      
      setSession(data.session)
      showToast('Connexion réussie', 'success')
      // setAuthLoading(false) removed
      return { success: true }
    } catch (error) {
      console.error('Login catch error:', error)
      // setAuthLoading(false) removed
      return { error: 'Email ou mot de passe incorrect (Erreur inattendue)' }
    }
  }

  // Logout handler
  const handleLogout = async () => {
    if (!window.confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) return
    
    try {
      await supabase.auth.signOut()
      setSession(null)
      setShowAvatarMenu(false)
      showToast('Déconnexion réussie', 'success')
    } catch (error) {
      showToast('Erreur lors de la déconnexion', 'error')
    }
  }

  // Format date to French format (DD/MM/YYYY)
  const formatDateFR = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  }

  // Fetch declarations from Supabase
  const fetchDeclarations = async () => {
    try {
      setLoading(true)
      const { data, error } = await supabase
        .from('declarations')
        .select('*')
        .order('incident_date', { ascending: false })

      if (error) throw error
      setDeclarations(data || [])
    } catch (error) {
      console.error('Erreur de chargement:', error.message)
      showToast('Erreur de chargement des données', 'error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (session) {
      fetchDeclarations()
    }
  }, [session])

  // Document type mapping
  const getDocName = (id) => {
    const types = {
      1: "CNI",
      2: "Passeport",
      3: "Permis",
      4: "Extrait",
      5: "Autre"
    }
    return types[id] || "Inconnu"
  }

  // Calculate statistics
  const stats = useMemo(() => ({
    total: declarations.length,
    found: declarations.filter(d => d.status === 'RETROUVE').length,
    pending: declarations.filter(d => d.status === 'EN_ATTENTE').length,
    lost: declarations.filter(d => d.status !== 'RETROUVE').length
  }), [declarations])

  // Recent pending declarations for notifications
  const recentPendingDeclarations = useMemo(() => {
    return declarations
      .filter(d => d.status === 'EN_ATTENTE')
      .slice(0, 3)
  }, [declarations])

  // Users data - group by user_id
  const usersData = useMemo(() => {
    const userMap = new Map()
    
    declarations.forEach(d => {
      const userId = d.user_id || 'unknown'
      if (!userMap.has(userId)) {
        userMap.set(userId, {
          userId,
          count: 0,
          lastActivity: null
        })
      }
      const user = userMap.get(userId)
      user.count++
      const incidentDate = new Date(d.incident_date)
      if (!user.lastActivity || incidentDate > user.lastActivity) {
        user.lastActivity = incidentDate
      }
    })
    
    return Array.from(userMap.values()).sort((a, b) => b.count - a.count)
  }, [declarations])

  // Top locations for reports
  const topLocations = useMemo(() => {
    const locationMap = new Map()
    
    declarations.forEach(d => {
      const location = d.incident_location || 'Non spécifié'
      locationMap.set(location, (locationMap.get(location) || 0) + 1)
    })
    
    return Array.from(locationMap.entries())
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3)
      .map(([name, count]) => ({ name, count, percentage: Math.round((count / declarations.length) * 100) }))
  }, [declarations])

  // Filter declarations based on search
  const filteredDeclarations = declarations.filter(item => {
    const query = searchQuery.toLowerCase()
    return (
      getDocName(item.document_type_id).toLowerCase().includes(query) ||
      (item.incident_location && item.incident_location.toLowerCase().includes(query)) ||
      (item.description && item.description.toLowerCase().includes(query))
    )
  })

  // Delete declaration handler
  const handleDelete = async (id) => {
    if (!window.confirm('Êtes-vous sûr de vouloir supprimer cette déclaration ? Cette action est irréversible.')) return
    
    try {
      const { error } = await supabase
        .from('declarations')
        .delete()
        .eq('id', id)
      
      if (error) throw error
      setDeclarations(declarations.filter(d => d.id !== id))
      showToast('Déclaration supprimée avec succès', 'success')
    } catch (error) {
      showToast('Erreur lors de la suppression: ' + error.message, 'error')
    }
  }

  // Delete user (all their declarations) handler
  const handleDeleteUser = async (userId) => {
    if (!window.confirm('Voulez-vous supprimer toutes les données de cet utilisateur ? Cette action est irréversible.')) return
    
    try {
      // Count declarations before deletion for feedback
      const userDeclarations = declarations.filter(d => d.user_id === userId)
      const count = userDeclarations.length
      
      const { error, count: deletedCount } = await supabase
        .from('declarations')
        .delete()
        .eq('user_id', userId)
        .select('id', { count: 'exact' })
      
      if (error) {
        console.error('Erreur Supabase lors de la suppression:', error)
        alert(`Échec de la suppression dans la base de données.\n\nErreur: ${error.message}\n\nVérifiez vos politiques RLS dans Supabase.`)
        showToast('Erreur lors de la suppression: ' + error.message, 'error')
        return
      }
      
      // Update local state
      setDeclarations(declarations.filter(d => d.user_id !== userId))
      showToast(`${count} déclaration(s) supprimée(s) avec succès`, 'success')
    } catch (error) {
      console.error('Erreur inattendue lors de la suppression:', error)
      alert(`Une erreur inattendue s'est produite.\n\nErreur: ${error.message}`)
      showToast('Erreur lors de la suppression: ' + error.message, 'error')
    }
  }

  // Handle settings save (dummy)
  const handleSaveSettings = () => {
    showToast('Paramètres enregistrés avec succès', 'success')
  }

  // Export PDF Report
  const handleExportPDF = () => {
    const doc = new jsPDF()
    const pageWidth = doc.internal.pageSize.getWidth()
    
    // Title
    doc.setFontSize(20)
    doc.setTextColor(79, 70, 229) // Primary color
    doc.text('Rapport Mensuel - PertePiece', pageWidth / 2, 20, { align: 'center' })
    
    // Date
    doc.setFontSize(10)
    doc.setTextColor(107, 114, 128) // Gray
    doc.text(`Généré le ${new Date().toLocaleDateString('fr-FR')}`, pageWidth / 2, 28, { align: 'center' })
    
    // Stats Summary
    doc.setFontSize(14)
    doc.setTextColor(17, 24, 39) // Dark
    doc.text('Résumé des statistiques', 14, 45)
    
    doc.setFontSize(11)
    doc.setTextColor(55, 65, 81)
    doc.text(`• Total des déclarations: ${stats.total}`, 20, 55)
    doc.text(`• Objets retrouvés: ${stats.found}`, 20, 63)
    doc.text(`• En attente: ${stats.pending}`, 20, 71)
    
    // Found Items Table
    const foundItems = declarations.filter(d => d.status === 'RETROUVE')
    
    if (foundItems.length > 0) {
      doc.setFontSize(14)
      doc.setTextColor(17, 24, 39)
      doc.text('Objets Retrouvés', 14, 90)
      
      const tableData = foundItems.map(item => [
        getDocName(item.document_type_id),
        formatDateFR(item.incident_date),
        item.incident_location || '-',
        item.description?.substring(0, 30) + (item.description?.length > 30 ? '...' : '') || '-'
      ])
      
      doc.autoTable({
        startY: 95,
        head: [['Type', 'Date', 'Lieu', 'Description']],
        body: tableData,
        styles: { fontSize: 9, cellPadding: 3 },
        headStyles: { fillColor: [79, 70, 229], textColor: 255 },
        alternateRowStyles: { fillColor: [249, 250, 251] }
      })
    } else {
      doc.setFontSize(11)
      doc.setTextColor(107, 114, 128)
      doc.text('Aucun objet retrouvé pour le moment.', 14, 90)
    }
    
    // Footer
    const pageHeight = doc.internal.pageSize.getHeight()
    doc.setFontSize(8)
    doc.setTextColor(156, 163, 175)
    doc.text('PertePiece Admin - Rapport automatique', pageWidth / 2, pageHeight - 10, { align: 'center' })
    
    // Download
    doc.save(`rapport-pertepiece-${new Date().toISOString().split('T')[0]}.pdf`)
    showToast('Rapport PDF téléchargé avec succès', 'success')
  }

  const menuItems = [
    { id: 'dashboard', icon: LayoutDashboard, label: 'Tableau de bord' },
    { id: 'users', icon: Users, label: 'Utilisateurs' },
    { id: 'reports', icon: FileText, label: 'Rapports' },
    { id: 'settings', icon: Settings, label: 'Paramètres' },
  ]

  // Get header info based on active tab
  const getHeaderInfo = () => {
    switch (activeTab) {
      case 'dashboard':
        return { title: 'Tableau de bord', subtitle: 'Gérez les déclarations de perte' }
      case 'users':
        return { title: 'Utilisateurs', subtitle: 'Activité des utilisateurs' }
      case 'reports':
        return { title: 'Rapports', subtitle: 'Statistiques et analyses' }
      case 'settings':
        return { title: 'Paramètres', subtitle: 'Configuration de l\'application' }
      default:
        return { title: 'Tableau de bord', subtitle: '' }
    }
  }

  const headerInfo = getHeaderInfo()

  // Show loading while checking auth
  if (authLoading && !session) {
    return (
      <div className="min-h-screen bg-linear-to-br from-primary-500 via-primary-600 to-primary-700 flex items-center justify-center">
        <div className="text-center">
          <RefreshCw className="w-10 h-10 text-white animate-spin mx-auto mb-4" />
          <p className="text-white/80">Chargement...</p>
        </div>
      </div>
    )
  }

  // Show login page if not authenticated
  if (!session) {
    return <LoginPage onLogin={handleLogin} isLoading={authLoading} />
  }

  // Render Dashboard Content
  const renderDashboard = () => (
    <div className="animate-fade-in">
      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {/* Total Reports */}
        <div className="stat-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Total Déclarations</p>
              <p className="text-3xl font-bold text-gray-900 dark:text-white mt-1">{stats.total}</p>
            </div>
            <div className="w-14 h-14 bg-linear-to-br from-primary-100 to-primary-200 rounded-2xl flex items-center justify-center">
              <FileText className="w-7 h-7 text-primary-600" />
            </div>
          </div>
          <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <span className="text-xs text-gray-500 dark:text-gray-400">Toutes les déclarations enregistrées</span>
          </div>
        </div>

        {/* Found Items */}
        <div className="stat-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Objets Retrouvés</p>
              <p className="text-3xl font-bold text-emerald-600 mt-1">{stats.found}</p>
            </div>
            <div className="w-14 h-14 bg-linear-to-br from-emerald-100 to-emerald-200 rounded-2xl flex items-center justify-center">
              <CheckCircle2 className="w-7 h-7 text-emerald-600" />
            </div>
          </div>
          <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div className="flex items-center gap-2">
              <span className="badge badge-success">RETROUVÉ</span>
              <span className="text-xs text-gray-500 dark:text-gray-400">Documents récupérés</span>
            </div>
          </div>
        </div>

        {/* Pending Items */}
        <div className="stat-card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-500 dark:text-gray-400">En Attente</p>
              <p className="text-3xl font-bold text-amber-600 mt-1">{stats.pending}</p>
            </div>
            <div className="w-14 h-14 bg-linear-to-br from-amber-100 to-amber-200 rounded-2xl flex items-center justify-center">
              <Clock className="w-7 h-7 text-amber-600" />
            </div>
          </div>
          <div className="mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
            <div className="flex items-center gap-2">
              <span className="badge badge-warning">EN ATTENTE</span>
              <span className="text-xs text-gray-500 dark:text-gray-400">À traiter</span>
            </div>
          </div>
        </div>
      </div>

      {/* Table Section */}
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
        {/* Table Header */}
        <div className="px-6 py-4 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Déclarations récentes</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">{filteredDeclarations.length} résultat(s)</p>
          </div>
          
          <div className="flex items-center gap-3">
            {/* Search Filter */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500" />
              <input
                type="text"
                placeholder="Filtrer par nom, lieu..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10 pr-4 py-2 w-64 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:border-primary-300 dark:focus:border-primary-500 transition-all"
              />
            </div>
            
            {/* Refresh Button */}
            <button
              onClick={fetchDeclarations}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-primary-500 hover:bg-primary-600 text-white rounded-xl font-medium text-sm transition-colors disabled:opacity-50"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              Actualiser
            </button>
          </div>
        </div>

        {/* Table Content */}
        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="flex flex-col items-center gap-4">
              <RefreshCw className="w-8 h-8 text-primary-500 animate-spin" />
              <p className="text-gray-500 dark:text-gray-400">Chargement des données...</p>
            </div>
          </div>
        ) : filteredDeclarations.length === 0 ? (
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <FileSearch className="w-10 h-10 text-gray-400" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Aucune déclaration</h4>
              <p className="text-gray-500 dark:text-gray-400 max-w-sm">
                {searchQuery 
                  ? "Aucun résultat ne correspond à votre recherche." 
                  : "Il n'y a pas encore de déclarations enregistrées."}
              </p>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Photo</th>
                  <th>Type</th>
                  <th>Date</th>
                  <th>Lieu</th>
                  <th>Statut</th>
                  <th>Description</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredDeclarations.map((item) => (
                  <tr key={item.id}>
                    <td>
                      {item.image_url ? (
                        <img 
                          src={item.image_url} 
                          alt="Preuve" 
                          className="w-12 h-12 rounded-xl object-cover border border-gray-200"
                        />
                      ) : (
                        <div className="w-12 h-12 bg-gray-100 rounded-xl flex items-center justify-center">
                          <FileText className="w-5 h-5 text-gray-400" />
                        </div>
                      )}
                    </td>
                    <td>
                      <span className="font-semibold text-gray-900 dark:text-white">
                        {getDocName(item.document_type_id)}
                      </span>
                    </td>
                    <td className="text-gray-600 dark:text-gray-300">{formatDateFR(item.incident_date)}</td>
                    <td className="text-gray-600 dark:text-gray-300">{item.incident_location}</td>
                    <td>
                      <span className={`badge ${
                        item.status === 'RETROUVE' ? 'badge-success' : 'badge-warning'
                      }`}>
                        {item.status === 'RETROUVE' ? 'Retrouvé' : 'En attente'}
                      </span>
                    </td>
                    <td className="max-w-xs truncate text-gray-600 dark:text-gray-300">{item.description}</td>
                    <td className="text-right">
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="p-2 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-500 transition-colors"
                        title="Supprimer"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )

  // Render Users Content with delete action
  const renderUsers = () => (
    <div className="animate-fade-in">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 dark:border-gray-700">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Liste des utilisateurs</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400">{usersData.length} utilisateur(s) actif(s)</p>
        </div>

        {usersData.length === 0 ? (
          <div className="flex items-center justify-center py-20">
            <div className="text-center">
              <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <Users className="w-10 h-10 text-gray-400" />
              </div>
              <h4 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Aucun utilisateur</h4>
              <p className="text-gray-500 dark:text-gray-400">Aucune déclaration n'a été enregistrée.</p>
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Utilisateur</th>
                  <th>ID Utilisateur</th>
                  <th>Nombre de déclarations</th>
                  <th>Dernière activité</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {usersData.map((user, index) => (
                  <tr key={user.userId}>
                    <td>
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-linear-to-br from-primary-400 to-primary-600 rounded-full flex items-center justify-center text-white font-semibold">
                          <User className="w-5 h-5" />
                        </div>
                        <span className="font-medium text-gray-900">Utilisateur {index + 1}</span>
                      </div>
                    </td>
                    <td>
                      <code className="text-xs bg-gray-100 px-2 py-1 rounded-lg text-gray-600">
                        {user.userId.length > 12 ? `${user.userId.substring(0, 12)}...` : user.userId}
                      </code>
                    </td>
                    <td>
                      <div className="flex items-center gap-2">
                        <span className="text-lg font-bold text-primary-600">{user.count}</span>
                        <span className="text-sm text-gray-500">déclaration(s)</span>
                      </div>
                    </td>
                    <td className="text-gray-600">
                      {user.lastActivity ? formatDateFR(user.lastActivity) : '-'}
                    </td>
                    <td className="text-right">
                      <button
                        onClick={() => handleDeleteUser(user.userId)}
                        className="p-2 rounded-lg hover:bg-red-50 text-gray-400 hover:text-red-500 transition-colors"
                        title="Supprimer l'utilisateur"
                      >
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )

  // Render Reports Content
  const renderReports = () => {
    const foundPercentage = stats.total > 0 ? Math.round((stats.found / stats.total) * 100) : 0
    const lostPercentage = 100 - foundPercentage

    return (
      <div className="animate-fade-in space-y-6">
        {/* Reports Header with Download Button */}
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Statistiques et Analyses</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">Visualisez les données de vos déclarations</p>
          </div>
          <button
            onClick={handleExportPDF}
            className="flex items-center gap-2 px-4 py-2.5 bg-primary-500 hover:bg-primary-600 text-white rounded-xl font-medium text-sm transition-colors shadow-lg shadow-primary-500/30"
          >
            <Download className="w-4 h-4" />
            📄 Télécharger le rapport
          </button>
        </div>

        {/* Lost vs Found Breakdown */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-6">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-linear-to-br from-primary-100 to-primary-200 dark:from-primary-900/50 dark:to-primary-800/50 rounded-xl flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-primary-600 dark:text-primary-400" />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Répartition Perdus vs Retrouvés</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">Sur un total de {stats.total} déclaration(s)</p>
            </div>
          </div>

          <div className="space-y-4">
            {/* Lost */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Perdus / En attente</span>
                <span className="text-sm font-bold text-amber-600">{lostPercentage}%</span>
              </div>
              <div className="progress-bar">
                <div 
                  className="progress-fill bg-linear-to-r from-amber-400 to-amber-500"
                  style={{ width: `${lostPercentage}%` }}
                />
              </div>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{stats.lost} déclaration(s)</p>
            </div>

            {/* Found */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Retrouvés</span>
                <span className="text-sm font-bold text-emerald-600">{foundPercentage}%</span>
              </div>
              <div className="progress-bar">
                <div 
                  className="progress-fill bg-linear-to-r from-emerald-400 to-emerald-500"
                  style={{ width: `${foundPercentage}%` }}
                />
              </div>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">{stats.found} déclaration(s)</p>
            </div>
          </div>
        </div>

        {/* Top Locations */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-6">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-linear-to-br from-accent-100 to-accent-200 dark:from-accent-900/50 dark:to-accent-800/50 rounded-xl flex items-center justify-center">
              <MapPin className="w-5 h-5 text-accent-600 dark:text-accent-400" />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Top 3 des lieux les plus fréquents</h3>
              <p className="text-sm text-gray-500 dark:text-gray-400">Zones avec le plus de pertes signalées</p>
            </div>
          </div>

          {topLocations.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500 dark:text-gray-400">Aucune donnée de localisation disponible</p>
            </div>
          ) : (
            <div className="space-y-4">
              {topLocations.map((location, index) => (
                <div key={location.name} className="flex items-center gap-4">
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-white ${
                    index === 0 ? 'bg-linear-to-br from-amber-400 to-amber-500' :
                    index === 1 ? 'bg-linear-to-br from-gray-400 to-gray-500' :
                    'bg-linear-to-br from-orange-300 to-orange-400'
                  }`}>
                    {index + 1}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center justify-between mb-1">
                      <span className="font-medium text-gray-900 dark:text-white">{location.name}</span>
                      <span className="text-sm text-gray-500 dark:text-gray-400">{location.count} déclaration(s)</span>
                    </div>
                    <div className="progress-bar h-2">
                      <div 
                        className="progress-fill bg-linear-to-r from-primary-400 to-primary-500"
                        style={{ width: `${location.percentage}%` }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    )
  }

  // Render Settings Content with functional logout
  const renderSettings = () => (
    <div className="animate-fade-in">
      <div className="max-w-2xl">
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-6">Paramètres du compte</h3>
          
          <div className="space-y-6">
            {/* Admin Name */}
            <div>
              <label className="form-label">Nom de l'administrateur</label>
              <input
                type="text"
                value={adminName}
                onChange={(e) => setAdminName(e.target.value)}
                className="form-input"
                placeholder="Votre nom"
              />
            </div>

            {/* Notification Email */}
            <div>
              <label className="form-label">Email de notification</label>
              <input
                type="email"
                value={notificationEmail}
                onChange={(e) => setNotificationEmail(e.target.value)}
                className="form-input"
                placeholder="email@exemple.com"
              />
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-2">Les alertes seront envoyées à cette adresse</p>
            </div>

            {/* Save Button */}
            <button
              onClick={handleSaveSettings}
              className="flex items-center gap-2 px-6 py-3 bg-primary-500 hover:bg-primary-600 text-white rounded-xl font-medium transition-colors"
            >
              <Save className="w-5 h-5" />
              Enregistrer les modifications
            </button>
          </div>
        </div>

        {/* Logout Section */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-6 mt-6">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Session</h3>
          <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
            Connecté en tant que: <span className="font-medium text-gray-700 dark:text-gray-300">{session?.user?.email}</span>
          </p>
          <button
            onClick={handleLogout}
            className="flex items-center gap-2 px-6 py-3 bg-red-50 hover:bg-red-100 text-red-600 rounded-xl font-medium transition-colors"
          >
            <LogOut className="w-5 h-5" />
            Se déconnecter
          </button>
        </div>
      </div>
    </div>
  )

  // Render content based on active tab
  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard':
        return renderDashboard()
      case 'users':
        return renderUsers()
      case 'reports':
        return renderReports()
      case 'settings':
        return renderSettings()
      default:
        return renderDashboard()
    }
  }

  return (
    <div className="flex min-h-screen bg-background">
      {/* Toast Notification */}
      {toast.show && (
        <Toast 
          message={toast.message} 
          type={toast.type} 
          onClose={() => setToast({ ...toast, show: false })} 
        />
      )}

      {/* Sidebar */}
      <aside className="fixed left-0 top-0 h-full w-72 bg-white dark:bg-gray-900 border-r border-gray-200 dark:border-gray-700 flex flex-col z-50">
        {/* Logo */}
        <div className="p-6 border-b border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-linear-to-br from-primary-500 to-primary-600 rounded-xl flex items-center justify-center shadow-lg shadow-primary-500/30">
              <FileSearch className="w-5 h-5 text-white" />
            </div>
            <div>
            <h1 className="text-lg font-bold text-gray-900 dark:text-white">PertePiece</h1>
              <p className="text-xs text-gray-500 dark:text-gray-400">Administration</p>
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`sidebar-link w-full ${activeTab === item.id ? 'active' : ''}`}
            >
              <item.icon className="w-5 h-5" />
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        {/* User Section */}
        <div className="p-4 border-t border-gray-100 dark:border-gray-800">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 dark:bg-gray-800">
            <div className="w-10 h-10 bg-linear-to-br from-accent-400 to-accent-500 rounded-full flex items-center justify-center text-white font-semibold">
              {adminName.charAt(0).toUpperCase()}
            </div>
            <div className="flex-1">
              <p className="text-sm font-medium text-gray-900 dark:text-white">{adminName}</p>
              <p className="text-xs text-gray-500 dark:text-gray-400">Administrateur</p>
            </div>
            <ChevronDown className="w-4 h-4 text-gray-400" />
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 ml-72">
        {/* Top Bar */}
        <header className="sticky top-0 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-200 dark:border-gray-700 z-40">
          <div className="flex items-center justify-between px-8 py-4">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{headerInfo.title}</h2>
              <p className="text-sm text-gray-500 dark:text-gray-400">{headerInfo.subtitle}</p>
            </div>
            
            <div className="flex items-center gap-4">
              {/* Global Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 dark:text-gray-500" />
                <input
                  type="text"
                  placeholder="Rechercher..."
                  className="pl-10 pr-4 py-2 w-64 bg-gray-100 dark:bg-gray-800 border-0 rounded-xl text-sm text-gray-900 dark:text-gray-100 placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-primary-500/20 focus:bg-white dark:focus:bg-gray-700 transition-all"
                />
              </div>
              
              {/* Dark Mode Toggle */}
              <button
                onClick={toggleDarkMode}
                className="p-2 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
                title={darkMode ? 'Mode clair' : 'Mode sombre'}
              >
                {darkMode ? (
                  <Sun className="w-5 h-5 text-amber-500" />
                ) : (
                  <Moon className="w-5 h-5 text-gray-600" />
                )}
              </button>
              
              {/* Notifications Bell */}
              <div className="relative" ref={notificationRef}>
                <button 
                  onClick={() => setShowNotifications(!showNotifications)}
                  className="relative p-2 rounded-xl hover:bg-gray-100 transition-colors"
                >
                  <Bell className="w-5 h-5 text-gray-600" />
                  {stats.pending > 0 && (
                    <span className="absolute top-1 right-1 w-2 h-2 bg-accent-500 rounded-full"></span>
                  )}
                </button>

                {/* Notification Dropdown */}
                {showNotifications && (
                  <div className="absolute right-0 top-full mt-2 w-80 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 py-2 animate-fade-in">
                    <div className="px-4 py-2 border-b border-gray-100 dark:border-gray-700">
                      <h4 className="font-semibold text-gray-900 dark:text-white">Notifications récentes</h4>
                      <p className="text-xs text-gray-500 dark:text-gray-400">{stats.pending} déclaration(s) en attente</p>
                    </div>
                    
                    {recentPendingDeclarations.length === 0 ? (
                      <div className="px-4 py-6 text-center">
                        <Bell className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                        <p className="text-sm text-gray-500 dark:text-gray-400">Aucune nouvelle déclaration</p>
                      </div>
                    ) : (
                      <div className="max-h-64 overflow-y-auto">
                        {recentPendingDeclarations.map((item) => (
                          <div 
                            key={item.id} 
                            className="px-4 py-3 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors cursor-pointer border-b border-gray-50 dark:border-gray-700 last:border-0"
                            onClick={() => {
                              setActiveTab('dashboard')
                              setShowNotifications(false)
                            }}
                          >
                            <div className="flex items-start gap-3">
                              <div className="w-8 h-8 bg-linear-to-br from-accent-100 to-accent-200 rounded-lg flex items-center justify-center shrink-0">
                                <FileText className="w-4 h-4 text-accent-600" />
                              </div>
                              <div className="flex-1 min-w-0">
                                <p className="text-sm font-medium text-gray-900 dark:text-white">
                                  Nouvelle {getDocName(item.document_type_id)} déclarée
                                </p>
                                <p className="text-xs text-gray-500 dark:text-gray-400 truncate">
                                  📍 {item.incident_location || 'Lieu non spécifié'}
                                </p>
                                <p className="text-xs text-gray-400 mt-1">
                                  {formatDateFR(item.incident_date)}
                                </p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                    
                    <div className="px-4 py-2 border-t border-gray-100">
                      <button 
                        onClick={() => {
                          setActiveTab('dashboard')
                          setShowNotifications(false)
                        }}
                        className="text-sm text-primary-600 hover:text-primary-700 font-medium"
                      >
                        Voir toutes les déclarations
                      </button>
                    </div>
                  </div>
                )}
              </div>
              
              {/* Avatar with dropdown */}
              <div className="relative" ref={avatarRef}>
                <div 
                  onClick={() => setShowAvatarMenu(!showAvatarMenu)}
                  className="w-10 h-10 bg-linear-to-br from-primary-500 to-primary-600 rounded-full flex items-center justify-center text-white font-semibold cursor-pointer hover:shadow-lg hover:shadow-primary-500/30 transition-all"
                >
                  {adminName.charAt(0).toUpperCase()}
                </div>

                {/* Avatar Dropdown */}
                {showAvatarMenu && (
                  <div className="absolute right-0 top-full mt-2 w-56 bg-white rounded-xl shadow-xl border border-gray-100 py-2 animate-fade-in">
                    <div className="px-4 py-3 border-b border-gray-100">
                      <p className="font-medium text-gray-900">{adminName}</p>
                      <p className="text-xs text-gray-500 truncate">{session?.user?.email}</p>
                    </div>
                    
                    <button
                      onClick={() => {
                        setActiveTab('settings')
                        setShowAvatarMenu(false)
                      }}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 transition-colors flex items-center gap-2"
                    >
                      <Settings className="w-4 h-4" />
                      Paramètres
                    </button>
                    
                    <button
                      onClick={handleLogout}
                      className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
                    >
                      <LogOut className="w-4 h-4" />
                      Se déconnecter
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Dynamic Content */}
        <div className="p-8">
          {renderContent()}
        </div>
      </main>
    </div>
  )
}

export default App