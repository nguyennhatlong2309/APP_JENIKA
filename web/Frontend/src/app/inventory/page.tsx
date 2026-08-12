'use client';

import { Suspense, useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Pagination from '@/components/ui/Pagination';
import { CategoryItem, UnitItem, GroupItem, ProductItem } from '@/types';
import { productService } from '@/services/productService';
import { formatVND } from '@/lib/utils';

function InventoryContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<'dang-kinh-doanh' | 'bi-an'>('dang-kinh-doanh');
  const [searchQuery, setSearchQuery] = useState('');

  // Edit mode states
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingProductId, setEditingProductId] = useState<number | null>(null);

  // Group search select states
  const [isGroupDropdownOpen, setIsGroupDropdownOpen] = useState(false);
  const [groupSearchQuery, setGroupSearchQuery] = useState('');

  // Small new group modal states
  const [isNewGroupModalOpen, setIsNewGroupModalOpen] = useState(false);
  const [newGroupName, setNewGroupName] = useState('');
  const [isCreatingGroup, setIsCreatingGroup] = useState(false);

  // Metadata list from DB
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [units, setUnits] = useState<UnitItem[]>([]);
  const [groups, setGroups] = useState<GroupItem[]>([]);

  // Checkbox filters
  const [selectedCategories, setSelectedCategories] = useState<number[]>([]);
  const [selectedGroups, setSelectedGroups] = useState<number[]>([]);
  const [selectedStatuses, setSelectedStatuses] = useState<string[]>([]);

  // Checklist searches
  const [groupSearch, setGroupSearch] = useState('');

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  // Inline editing states
  const [editingCategoryId, setEditingCategoryId] = useState<number | null>(null);
  const [editCategoryName, setEditCategoryName] = useState('');
  const [editingGroupId, setEditingGroupId] = useState<number | null>(null);
  const [editGroupName, setEditGroupName] = useState('');

  // Form fields
  const [productName, setProductName] = useState('');
  const [categoryId, setCategoryId] = useState<string>('');
  const [unitId, setUnitId] = useState<string>('');
  const [groupId, setGroupId] = useState<string>('');
  const [costPrice, setCostPrice] = useState<number>(0);
  const [sellingPrice, setSellingPrice] = useState<number>(0);
  const [initialStock, setInitialStock] = useState<number>(10);
  const [warningStock, setWarningStock] = useState<number>(5);
  const [ghiChu, setGhiChu] = useState('');

  // Delete confirm modal states
  const [productToDelete, setProductToDelete] = useState<ProductItem | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  // Data list
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalError, setModalError] = useState<string | null>(null);

  // Pagination & counts states
  const [totalPages, setTotalPages] = useState(0);
  const [totalFilteredItems, setTotalFilteredItems] = useState(0);
  const [stats, setStats] = useState({
    totalItems: 0,
    totalValue: 0,
    lowStockCount: 0,
    activeCount: 0,
    deletedCount: 0
  });

  // Debounced search query
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
      setCurrentPage(1);
    }, 400);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  const fetchStats = async () => {
    try {
      const statsData = await productService.getProductStats();
      setStats(statsData);
    } catch (err) {
      console.error("Error loading product stats:", err);
    }
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const isDeleted = activeTab === 'bi-an';

      const pageData = await productService.getProductsPage({
        page: currentPage - 1, // Spring Boot is 0-indexed
        size: itemsPerPage,
        biXoa: isDeleted,
        search: debouncedSearchQuery.trim() || undefined,
        categoryIds: selectedCategories.length > 0 ? selectedCategories : undefined,
        groupIds: selectedGroups.length > 0 ? selectedGroups : undefined,
        statuses: selectedStatuses.length > 0 ? selectedStatuses : undefined
      });

      setProducts(pageData.content);
      setTotalPages(pageData.totalPages);
      setTotalFilteredItems(pageData.totalElements);
    } catch (err) {
      console.error("Error loading products page:", err);
    } finally {
      setLoading(false);
    }
  };

  // Fetch initial metadata and stats
  const loadInitialData = async () => {
    try {
      setLoading(true);
      // Fetch metadata and stats in parallel
      const [cats, uns, gps] = await Promise.all([
        productService.getCategories(),
        productService.getUnits(),
        productService.getGroups(),
        fetchStats()
      ]);

      setCategories(cats);
      if (cats.length > 0 && !categoryId) setCategoryId(cats[0].id.toString());

      setUnits(uns);
      if (uns.length > 0 && !unitId) setUnitId(uns[0].id.toString());

      setGroups(gps);
      if (gps.length > 0 && !groupId) setGroupId(gps[0].id.toString());
    } catch (err) {
      console.error("Error loading initial inventory data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInitialData();
  }, []);

  // Fetch page products when parameters change
  useEffect(() => {
    fetchProducts();
  }, [currentPage, itemsPerPage, debouncedSearchQuery, selectedCategories, selectedGroups, selectedStatuses, activeTab]);

  // Open modal if query is add=true
  useEffect(() => {
    if (searchParams.get('add') === 'true') {
      setIsModalOpen(true);
    }
  }, [searchParams]);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closeModal();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);



  // Close group dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('.group-dropdown-container')) {
        setIsGroupDropdownOpen(false);
      }
    };
    if (isGroupDropdownOpen) {
      window.addEventListener('click', handleClickOutside);
    }
    return () => window.removeEventListener('click', handleClickOutside);
  }, [isGroupDropdownOpen]);

  const closeModal = () => {
    setIsModalOpen(false);
    setModalError(null);
    setIsEditMode(false);
    setEditingProductId(null);

    // Reset form fields
    setProductName('');
    setCostPrice(0);
    setSellingPrice(0);
    setInitialStock(10);
    setWarningStock(5);
    setGhiChu('');
    setCategoryId(categories.length > 0 ? categories[0].id.toString() : '');
    setUnitId(units.length > 0 ? units[0].id.toString() : '');
    setGroupId(groups.length > 0 ? groups[0].id.toString() : '');

    router.replace('/inventory');
  };

  const handleEditClick = (product: ProductItem) => {
    setIsEditMode(true);
    setEditingProductId(product.id);
    setProductName(product.tenSanPham);
    setCategoryId(product.danhMuc ? product.danhMuc.id.toString() : '');
    setUnitId(product.donViTinh ? product.donViTinh.id.toString() : '');
    setGroupId(product.nhomSanPham ? product.nhomSanPham.id.toString() : '');
    setCostPrice(product.giaNhapHienTai || 0);
    setSellingPrice(product.giaBanHienTai || 0);
    setInitialStock(product.soLuongTon || 0);
    setWarningStock(product.canhBaoTonKho || 0);
    setGhiChu(product.ghiChu || '');
    setIsModalOpen(true);
  };

  const handleCreateNewGroup = async () => {
    if (!newGroupName.trim()) return;
    setIsCreatingGroup(true);
    try {
      const createdGroup = await productService.createGroup({ tenNhom: newGroupName.trim() });
      setGroups(prev => [...prev, createdGroup]);
      setGroupId(createdGroup.id.toString());
      setProductName(createdGroup.tenNhom + " ");
      setNewGroupName('');
      setIsNewGroupModalOpen(false);
    } catch (err) {
      console.error(err);
      alert("Không thể tạo nhóm sản phẩm mới.");
    } finally {
      setIsCreatingGroup(false);
    }
  };

  const handleSubmitProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!productName.trim()) {
      setModalError("Tên sản phẩm không được để trống.");
      return;
    }

    const payload = {
      tenSanPham: productName.trim(),
      giaNhapHienTai: costPrice,
      giaBanHienTai: sellingPrice,
      soLuongTon: initialStock,
      canhBaoTonKho: warningStock,
      danhMuc: categoryId ? { id: parseInt(categoryId) } : null,
      donViTinh: unitId ? { id: parseInt(unitId) } : null,
      nhomSanPham: groupId ? { id: parseInt(groupId) } : null,
      biXoa: false,
      ghiChu: ghiChu.trim()
    };

    try {
      if (isEditMode && editingProductId) {
        await productService.updateProduct(editingProductId, payload);
      } else {
        await productService.createProduct(payload);
      }
      closeModal();
      fetchProducts();
      fetchStats();
    } catch (err) {
      console.error(err);
      setModalError("Lỗi kết nối máy chủ hoặc lỗi khi lưu sản phẩm.");
    }
  };

  const handleDeleteProduct = (product: ProductItem) => {
    setProductToDelete(product);
    setDeleteError(null);
  };

  const confirmDeleteProduct = async () => {
    if (!productToDelete) return;
    setIsDeleting(true);
    setDeleteError(null);
    try {
      await productService.deleteProduct(productToDelete.id);
      setProductToDelete(null);
      fetchProducts();
      fetchStats();
    } catch (err) {
      console.error(err);
      setDeleteError("Không thể ẩn sản phẩm. Vui lòng thử lại sau.");
    } finally {
      setIsDeleting(false);
    }
  };

  const handleRestoreProduct = async (id: number) => {
    try {
      await productService.restoreProduct(id);
      fetchProducts();
      fetchStats();
    } catch (err) {
      console.error(err);
      alert("Không thể khôi phục sản phẩm.");
    }
  };

  const handleSaveCategoryName = async (id: number) => {
    if (!editCategoryName.trim()) return;
    try {
      await productService.updateCategory(id, { tenDanhMuc: editCategoryName.trim() });
      setEditingCategoryId(null);
      loadInitialData();
      fetchProducts();
    } catch (err) {
      console.error(err);
      alert("Không thể cập nhật tên danh mục.");
    }
  };

  const handleSaveGroupName = async (id: number) => {
    if (!editGroupName.trim()) return;
    try {
      await productService.updateGroup(id, { tenNhom: editGroupName.trim() });
      setEditingGroupId(null);
      loadInitialData();
      fetchProducts();
    } catch (err) {
      console.error(err);
      alert("Không thể cập nhật tên nhóm sản phẩm.");
    }
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white tracking-wide">Danh mục Hàng hóa</h2>
        </div>
        <button
          onClick={() => {
            setIsEditMode(false);
            setEditingProductId(null);
            setProductName('');
            setCategoryId(categories.length > 0 ? categories[0].id.toString() : '');
            setUnitId(units.length > 0 ? units[0].id.toString() : '');
            setGroupId(groups.length > 0 ? groups[0].id.toString() : '');
            setCostPrice(0);
            setSellingPrice(0);
            setInitialStock(10);
            setWarningStock(5);
            setGhiChu('');
            setIsModalOpen(true);
          }}
          className="bg-primary text-on-primary px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs animate-in fade-in"
        >
          <span className="material-symbols-outlined text-base">add</span>
          <span>Thêm sản phẩm mới</span>
        </button>
      </div>

      {/* Stats Row (Reduced height & compact design) */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0">
        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg text-lg">inventory_2</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng số lượng tồn</p>
              <h3 className="text-sm font-bold text-on-surface">{stats.totalItems.toLocaleString()} Sản phẩm</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-secondary bg-secondary/10 p-2 rounded-lg text-lg">account_balance_wallet</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Giá trị kho (Giá nhập)</p>
              <h3 className="text-sm font-bold text-on-surface">{formatVND(stats.totalValue)}</h3>
            </div>
          </div>
        </div>

        <div className={`glass-card py-2.5 px-4 rounded-xl flex items-center justify-between transition-all ${stats.lowStockCount > 0 ? 'border-warning/30 hover:border-warning/50' : 'hover:border-primary/30'}`}>
          <div className="flex items-center gap-3">
            <span className={`material-symbols-outlined p-2 rounded-lg text-lg ${stats.lowStockCount > 0 ? 'text-warning bg-warning/10' : 'text-primary bg-primary/10'}`}>warning</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Cảnh báo hết hàng</p>
              <h3 className={`text-sm font-bold ${stats.lowStockCount > 0 ? 'text-warning' : 'text-on-surface'}`}>{stats.lowStockCount} Sản phẩm</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-tertiary bg-tertiary/10 p-2 rounded-lg text-lg">category</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Danh mục hoạt động</p>
              <h3 className="text-sm font-bold text-on-surface">{categories.length} Danh mục</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Main Grid Layout (Sidebar Filters + Table Panel) */}
      <div className="flex-1 flex flex-col lg:flex-row gap-6 items-stretch overflow-hidden mt-2 w-full">
        {/* Left Sidebar Filter Section */}
        <div className="w-full lg:w-72 flex-shrink-0 flex flex-col h-full overflow-hidden">
          <div className="glass-card p-4 rounded-xl flex flex-col h-full overflow-hidden space-y-4">
            <div className="flex justify-between items-center pb-2 border-b border-border-glass flex-shrink-0">
              <h4 className="text-xs font-bold text-primary flex items-center gap-1.5">
                <span className="material-symbols-outlined text-base">filter_alt</span>
                Bộ lọc nâng cao
              </h4>
              {(selectedCategories.length > 0 || selectedGroups.length > 0 || selectedStatuses.length > 0) && (
                <button
                  onClick={() => {
                    setSelectedCategories([]);
                    setSelectedGroups([]);
                    setSelectedStatuses([]);
                  }}
                  className="text-[10px] text-error hover:underline font-semibold cursor-pointer"
                >
                  Xóa lọc
                </button>
              )}
            </div>

            {/* Scrollable Container for all Checkbox Filter Sections */}
            <div className="flex-1 flex flex-col min-h-0 space-y-4">
              {/* Filter 1: Danh mục (Fixed size) */}
              <div className="flex-shrink-0 space-y-2">
                <p className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Danh mục</p>
                <div className="max-h-[110px] overflow-y-auto pr-1 space-y-1" data-lenis-prevent="">
                  {categories
                    .map(cat => {
                      const isSelected = selectedCategories.includes(cat.id);
                      const isEditing = editingCategoryId === cat.id;

                      return (
                        <div key={cat.id} className="flex items-center justify-between py-1 group/item">
                          {isEditing ? (
                            <div className="flex items-center gap-1 w-full">
                              <input
                                value={editCategoryName}
                                onChange={(e) => setEditCategoryName(e.target.value)}
                                className="w-full bg-surface-lowest border border-primary/50 rounded px-1.5 py-0.5 text-xs text-white outline-none focus:ring-1 focus:ring-primary"
                                autoFocus
                                onKeyDown={(e) => {
                                  if (e.key === 'Enter') handleSaveCategoryName(cat.id);
                                  if (e.key === 'Escape') setEditingCategoryId(null);
                                }}
                              />
                              <button
                                onClick={() => handleSaveCategoryName(cat.id)}
                                className="text-success hover:text-success/80 p-0.5 cursor-pointer"
                                title="Lưu"
                              >
                                <span className="material-symbols-outlined text-sm">done</span>
                              </button>
                              <button
                                onClick={() => setEditingCategoryId(null)}
                                className="text-error hover:text-error/80 p-0.5 cursor-pointer"
                                title="Hủy"
                              >
                                <span className="material-symbols-outlined text-sm">close</span>
                              </button>
                            </div>
                          ) : (
                            <>
                              <label className="flex items-center gap-2 cursor-pointer text-xs text-on-surface-variant hover:text-white flex-1 select-none py-0.5">
                                <input
                                  type="checkbox"
                                  checked={isSelected}
                                  onChange={() => {
                                    if (isSelected) {
                                      setSelectedCategories(selectedCategories.filter(id => id !== cat.id));
                                    } else {
                                      setSelectedCategories([...selectedCategories, cat.id]);
                                    }
                                    setCurrentPage(1);
                                  }}
                                  className="rounded border-border-glass text-primary focus:ring-primary/50 bg-surface-lowest w-3.5 h-3.5"
                                />
                                <span className="truncate max-w-[150px]" title={cat.tenDanhMuc}>
                                  {cat.tenDanhMuc}
                                </span>
                              </label>
                              <button
                                onClick={() => {
                                  setEditingCategoryId(cat.id);
                                  setEditCategoryName(cat.tenDanhMuc);
                                }}
                                className="opacity-0 group-hover/item:opacity-100 p-0.5 text-on-surface-variant hover:text-primary transition-all ml-1 cursor-pointer"
                                title="Sửa tên"
                              >
                                <span className="material-symbols-outlined text-xs">edit</span>
                              </button>
                            </>
                          )}
                        </div>
                      );
                    })}
                </div>
              </div>

              {/* Filter 2: Nhóm sản phẩm (Stretches dynamically to fill all remaining vertical space!) */}
              <div className="flex-1 flex flex-col min-h-0 space-y-2">
                <p className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider flex-shrink-0">Nhóm sản phẩm</p>
                <div className="relative mb-2 flex-shrink-0">
                  <span className="material-symbols-outlined absolute left-2 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                    search
                  </span>
                  <input
                    value={groupSearch}
                    onChange={(e) => setGroupSearch(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded py-1 pl-7 pr-2 text-xs text-white outline-none focus:ring-1 focus:ring-primary/40"
                    placeholder="Tìm nhóm..."
                    type="text"
                  />
                </div>
                <div className="flex-1 overflow-y-auto pr-1 space-y-1 min-h-0" data-lenis-prevent="">
                  {groups
                    .filter(g => g.tenNhom.toLowerCase().includes(groupSearch.toLowerCase()))
                    .map(g => {
                      const isSelected = selectedGroups.includes(g.id);
                      const isEditing = editingGroupId === g.id;

                      return (
                        <div key={g.id} className="flex items-center justify-between py-1 group/item">
                          {isEditing ? (
                            <div className="flex items-center gap-1 w-full">
                              <input
                                value={editGroupName}
                                onChange={(e) => setEditGroupName(e.target.value)}
                                className="w-full bg-surface-lowest border border-primary/50 rounded px-1.5 py-0.5 text-xs text-white outline-none focus:ring-1 focus:ring-primary"
                                autoFocus
                                onKeyDown={(e) => {
                                  if (e.key === 'Enter') handleSaveGroupName(g.id);
                                  if (e.key === 'Escape') setEditingGroupId(null);
                                }}
                              />
                              <button
                                onClick={() => handleSaveGroupName(g.id)}
                                className="text-success hover:text-success/80 p-0.5 cursor-pointer"
                                title="Lưu"
                              >
                                <span className="material-symbols-outlined text-sm">done</span>
                              </button>
                              <button
                                onClick={() => setEditingGroupId(null)}
                                className="text-error hover:text-error/80 p-0.5 cursor-pointer"
                                title="Hủy"
                              >
                                <span className="material-symbols-outlined text-sm">close</span>
                              </button>
                            </div>
                          ) : (
                            <>
                              <label className="flex items-center gap-2 cursor-pointer text-xs text-on-surface-variant hover:text-white flex-1 select-none py-0.5">
                                <input
                                  type="checkbox"
                                  checked={isSelected}
                                  onChange={() => {
                                    if (isSelected) {
                                      setSelectedGroups(selectedGroups.filter(id => id !== g.id));
                                    } else {
                                      setSelectedGroups([...selectedGroups, g.id]);
                                    }
                                    setCurrentPage(1);
                                  }}
                                  className="rounded border-border-glass text-primary focus:ring-primary/50 bg-surface-lowest w-3.5 h-3.5"
                                />
                                <span className="truncate max-w-[150px]" title={g.tenNhom}>
                                  {g.tenNhom}
                                </span>
                              </label>
                              <button
                                onClick={() => {
                                  setEditingGroupId(g.id);
                                  setEditGroupName(g.tenNhom);
                                }}
                                className="opacity-0 group-hover/item:opacity-100 p-0.5 text-on-surface-variant hover:text-primary transition-all ml-1 cursor-pointer"
                                title="Sửa tên"
                              >
                                <span className="material-symbols-outlined text-xs">edit</span>
                              </button>
                            </>
                          )}
                        </div>
                      );
                    })}
                </div>
              </div>

              {/* Filter 3: Tình trạng kho (Fixed size at bottom) */}
              <div className="flex-shrink-0 space-y-2 border-t border-border-glass pt-3">
                <p className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider">Tình trạng kho</p>
                <div className="space-y-1">
                  {['Còn hàng', 'Cảnh báo', 'Hết hàng'].map(status => {
                    const isSelected = selectedStatuses.includes(status);
                    const colorClass =
                      status === 'Còn hàng' ? 'text-success font-semibold' :
                        status === 'Cảnh báo' ? 'text-warning font-semibold' : 'text-error font-semibold';

                    return (
                      <div key={status} className="flex items-center py-0.5">
                        <label className="flex items-center gap-2 cursor-pointer text-xs text-on-surface-variant hover:text-white select-none py-0.5 w-full">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() => {
                              if (isSelected) {
                                setSelectedStatuses(selectedStatuses.filter(s => s !== status));
                              } else {
                                setSelectedStatuses([...selectedStatuses, status]);
                              }
                              setCurrentPage(1);
                            }}
                            className="rounded border-border-glass text-primary focus:ring-primary/50 bg-surface-lowest w-3.5 h-3.5"
                          />
                          <span className={colorClass}>{status}</span>
                        </label>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Main Table and Tabs Panel - Viewport Bound Flex Container */}
        <div className="flex-1 w-full flex flex-col h-full overflow-hidden glass-card rounded-xl">
          {/* Primary Business Tabs */}
          <div className="flex justify-between items-center p-4 border-b border-border-glass flex-wrap gap-4 flex-shrink-0 bg-white/1">
            <div className="flex gap-6">
              <button
                onClick={() => {
                  setActiveTab('dang-kinh-doanh');
                  setCurrentPage(1);
                }}
                className={`pb-2.5 font-bold text-sm transition-all border-b-2 relative cursor-pointer flex items-center gap-2 ${activeTab === 'dang-kinh-doanh'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-on-surface-variant hover:text-white'
                  }`}
              >
                <span>Hàng hóa đang kinh doanh</span>
                <span className="text-[10px] bg-primary/20 text-primary py-0.5 px-2 rounded-full">
                  {stats.activeCount}
                </span>
              </button>
              <button
                onClick={() => {
                  setActiveTab('bi-an');
                  setCurrentPage(1);
                }}
                className={`pb-2.5 font-bold text-sm transition-all border-b-2 relative cursor-pointer flex items-center gap-2 ${activeTab === 'bi-an'
                  ? 'border-primary text-primary'
                  : 'border-transparent text-on-surface-variant hover:text-white'
                  }`}
              >
                <span>Hàng hóa bị ẩn</span>
                <span className="text-[10px] bg-error/20 text-error py-0.5 px-2 rounded-full">
                  {stats.deletedCount}
                </span>
              </button>
            </div>
            <div className="flex items-center gap-4 flex-wrap">
              <div className="relative w-60">
                <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                  search
                </span>
                <input
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                  placeholder="Tìm tên, mã sản phẩm..."
                  type="text"
                />
              </div>
              <div className="text-xs text-on-surface-variant font-semibold">
                Hiển thị <span className="text-white font-bold">{totalFilteredItems}</span> sản phẩm
              </div>
            </div>
          </div>

          {/* Table Content - Internally Scrollable Wrapper */}
          <div className="flex-1 overflow-auto" data-lenis-prevent="">
            <table className="w-full text-left border-collapse relative">
              <thead className="sticky top-0 z-20 shadow-[0_1px_0_0_rgba(255,255,255,0.08)] bg-[#131929]">
                <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-widest">
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md">Mã</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md">Tên sản phẩm</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md">Đơn vị</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-right">Giá bán</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-right">Giá nhập</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-right">Tồn kho</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-right">Giá trị tồn</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-center">Trạng thái</th>
                  <th className="px-4 py-3 bg-[#131929] backdrop-blur-md text-center">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-glass">
                {loading ? (
                  <tr>
                    <td colSpan={9} className="px-6 py-12 text-center text-on-surface-variant text-xs font-semibold">
                      Đang tải danh sách hàng hóa...
                    </td>
                  </tr>
                ) : products.length === 0 ? (
                  <tr>
                    <td colSpan={9} className="px-6 py-12 text-center text-on-surface-variant text-xs font-semibold">
                      Không tìm thấy sản phẩm nào khớp với bộ lọc.
                    </td>
                  </tr>
                ) : (
                  products.map((p) => {
                    const isLow = p.soLuongTon <= p.canhBaoTonKho;
                    const isOutOfStock = p.soLuongTon <= 0 || p.trangThai === 'Hết hàng';
                    const isWarning = !isOutOfStock && (p.soLuongTon <= p.canhBaoTonKho || p.trangThai === 'Cảnh báo');
                    const giaTriTon = (p.soLuongTon || 0) * (p.giaNhapHienTai || 0);

                    return (
                      <tr
                        key={p.id}
                        className={`hover:bg-white/5 transition-all group ${!p.biXoa && isOutOfStock ? 'bg-error/5' :
                          !p.biXoa && isWarning ? 'bg-warning/5' : ''
                          }`}
                      >
                        {/* Mã */}
                        <td className="px-4 py-3 font-mono text-xs text-primary">SP-{p.id}</td>

                        {/* Tên */}
                        <td className="px-4 py-3 font-semibold text-white">
                          <div>
                            <span className="block">{p.tenSanPham}</span>
                            <div className="text-[9px] text-on-surface-variant font-normal mt-0.5 flex gap-2">
                              {p.danhMuc && (
                                <span className="bg-white/5 px-1 py-0.5 rounded">
                                  DM: {p.danhMuc.tenDanhMuc}
                                </span>
                              )}
                              {p.nhomSanPham && (
                                <span className="bg-white/5 px-1 py-0.5 rounded">
                                  Nhóm: {p.nhomSanPham.tenNhom}
                                </span>
                              )}
                            </div>
                            {p.ghiChu && (
                              <p className="text-[10px] text-[#a0aabf] italic mt-1 bg-white/5 px-2 py-1 rounded max-w-sm truncate" title={p.ghiChu}>
                                Ghi chú: {p.ghiChu}
                              </p>
                            )}
                          </div>
                        </td>

                        {/* Đơn vị */}
                        <td className="px-4 py-3 text-xs text-on-surface-variant">
                          {p.donViTinh ? p.donViTinh.tenDonVi : 'cái'}
                        </td>

                        {/* Giá bán */}
                        <td className="px-4 py-3 text-xs font-bold text-white font-mono text-right">
                          {formatVND(p.giaBanHienTai)}
                        </td>

                        {/* Giá nhập */}
                        <td className="px-4 py-3 text-xs text-on-surface-variant font-mono text-right">
                          {formatVND(p.giaNhapHienTai)}
                        </td>

                        {/* Tồn kho */}
                        <td className="px-4 py-3 text-right">
                          <div className="inline-block text-right">
                            <span className={`text-xs font-bold ${!p.biXoa && isOutOfStock ? 'text-error' :
                              !p.biXoa && isWarning ? 'text-warning' : 'text-on-surface'
                              }`}>
                              {p.soLuongTon}
                            </span>
                            {isLow && !p.biXoa && (
                              isOutOfStock ? (
                                <span className="text-[8px] bg-error/25 text-error border border-error/30 px-1 ml-1.5 rounded uppercase font-bold">Hết</span>
                              ) : (
                                <span className="text-[8px] bg-warning/25 text-warning border border-warning/30 px-1 ml-1.5 rounded uppercase font-bold">Yếu</span>
                              )
                            )}
                          </div>
                        </td>

                        {/* Giá trị tồn */}
                        <td className="px-4 py-3 text-xs font-mono text-right text-on-surface-variant">
                          {formatVND(giaTriTon)}
                        </td>

                        {/* Trạng thái */}
                        <td className="px-4 py-3 text-center">
                          <span className={`px-2 py-0.5 rounded-full text-[9px] font-bold ${p.trangThai === 'Còn hàng' ? 'bg-success/20 text-success' :
                            p.trangThai === 'Cảnh báo' ? 'bg-warning/20 text-warning' :
                              'bg-error/20 text-error'
                            }`}>
                            {p.trangThai}
                          </span>
                        </td>

                        {/* Thao tác */}
                        <td className="px-4 py-3 text-center">
                          <div className="flex items-center justify-center gap-2">
                            {!p.biXoa ? (
                              <>
                                <button
                                  onClick={() => handleEditClick(p)}
                                  className="p-1 text-on-surface-variant hover:text-primary transition-colors cursor-pointer rounded hover:bg-white/5 inline-flex items-center justify-center"
                                  title="Chỉnh sửa sản phẩm"
                                >
                                  <span className="material-symbols-outlined text-lg">edit</span>
                                </button>
                                <button
                                  onClick={() => handleDeleteProduct(p)}
                                  className="p-1 text-on-surface-variant hover:text-error transition-colors cursor-pointer rounded hover:bg-white/5 inline-flex items-center justify-center"
                                  title="Ẩn sản phẩm (Xóa mềm)"
                                >
                                  <span className="material-symbols-outlined text-lg">delete</span>
                                </button>
                              </>
                            ) : (
                              <button
                                onClick={() => handleRestoreProduct(p.id)}
                                className="p-1 text-on-surface-variant hover:text-success transition-colors cursor-pointer rounded hover:bg-white/5 inline-flex items-center justify-center"
                                title="Khôi phục hàng hóa"
                              >
                                <span className="material-symbols-outlined text-lg">restore</span>
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* Reusable Pagination Control - Anchored at the bottom of the flex column */}
          <div className="flex-shrink-0">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
              totalItems={totalFilteredItems}
              itemsPerPage={itemsPerPage}
              onItemsPerPageChange={(size) => {
                setItemsPerPage(size);
                setCurrentPage(1);
              }}
            />
          </div>
        </div>
      </div>

      {/* Add / Edit Product Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm">
          {/* Backdrop */}
          <div className="absolute inset-0" onClick={closeModal}></div>

          <div className="relative glass-card w-full max-w-4xl rounded-2xl shadow-2xl overflow-hidden z-50 animate-in fade-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-border-glass flex justify-between items-center bg-white/2">
              <div>
                <h3 className="text-lg font-bold text-primary">
                  {isEditMode ? 'Chỉnh sửa thông tin Sản phẩm' : 'Thêm sản phẩm mới vào Menu'}
                </h3>
                <p className="text-xs text-on-surface-variant">
                  {isEditMode ? 'Cập nhật lại các thông số và giá cả của sản phẩm.' : 'Nhập thông tin chi tiết của sản phẩm để đồng bộ cơ sở dữ liệu.'}
                </p>
              </div>
              <button
                className="p-2 rounded-full hover:bg-white/5 text-on-surface-variant hover:text-error transition-all cursor-pointer"
                onClick={closeModal}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            {modalError && (
              <div className="p-3 mx-6 mt-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleSubmitProduct}>
              <div className="p-8 grid grid-cols-1 md:grid-cols-3 gap-6 max-h-[500px] overflow-y-auto">

                {/* Group selection (Trường đầu tiên) */}
                <div className="space-y-2 md:col-span-1 relative group-dropdown-container">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Nhóm sản phẩm</label>
                  <div className="relative">
                    <button
                      type="button"
                      onClick={() => setIsGroupDropdownOpen(!isGroupDropdownOpen)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 text-left text-white flex justify-between items-center focus:ring-1 focus:ring-primary focus:outline-none transition-all cursor-pointer outline-none text-xs"
                    >
                      <span className="truncate">
                        {groups.find(g => g.id.toString() === groupId)?.tenNhom || '-- Chọn nhóm sản phẩm --'}
                      </span>
                      <span className="material-symbols-outlined text-sm transition-transform duration-200" style={{ transform: isGroupDropdownOpen ? 'rotate(180deg)' : 'none' }}>
                        keyboard_arrow_down
                      </span>
                    </button>

                    {isGroupDropdownOpen && (
                      <div className="absolute left-0 right-0 mt-1.5 z-[100] bg-[#1a2333] border border-border-glass rounded-xl shadow-2xl overflow-hidden p-2 flex flex-col space-y-2 max-h-[250px]">
                        {/* Search Input */}
                        <div className="relative">
                          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                            search
                          </span>
                          <input
                            type="text"
                            value={groupSearchQuery}
                            onChange={(e) => setGroupSearchQuery(e.target.value)}
                            className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs text-white focus:ring-1 focus:ring-primary/50 outline-none"
                            placeholder="Tìm kiếm nhóm..."
                            onClick={(e) => e.stopPropagation()}
                            autoFocus
                          />
                        </div>

                        {/* Options List */}
                        <div className="overflow-y-auto flex-1 max-h-[180px] space-y-0.5">
                          {/* Create New Group option */}
                          <button
                            type="button"
                            onClick={(e) => {
                              e.stopPropagation();
                              setIsNewGroupModalOpen(true);
                              setIsGroupDropdownOpen(false);
                            }}
                            className="w-full text-left px-3 py-2 rounded-lg text-xs font-semibold text-primary hover:bg-white/5 transition-all flex items-center gap-1.5 cursor-pointer"
                          >
                            <span className="material-symbols-outlined text-sm">add_circle</span>
                            <span>+ Tạo nhóm mới...</span>
                          </button>

                          {/* Matching Groups */}
                          {groups
                            .filter(g => g.tenNhom.toLowerCase().includes(groupSearchQuery.toLowerCase()))
                            .map(g => {
                              const isSelected = g.id.toString() === groupId;
                              return (
                                <button
                                  key={g.id}
                                  type="button"
                                  onClick={() => {
                                    setGroupId(g.id.toString());
                                    setProductName(g.tenNhom + " "); // Auto-fill
                                    setIsGroupDropdownOpen(false);
                                  }}
                                  className={`w-full text-left px-3 py-2 rounded-lg text-xs transition-all flex justify-between items-center cursor-pointer ${isSelected
                                    ? 'bg-primary/20 text-primary font-bold'
                                    : 'text-on-surface-variant hover:bg-white/5 hover:text-white'
                                    }`}
                                >
                                  <span>{g.tenNhom}</span>
                                  {isSelected && <span className="material-symbols-outlined text-sm">done</span>}
                                </button>
                              );
                            })}

                          {groups.filter(g => g.tenNhom.toLowerCase().includes(groupSearchQuery.toLowerCase())).length === 0 && (
                            <div className="text-center text-[10px] text-on-surface-variant py-4">
                              Không tìm thấy nhóm phù hợp.
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {/* Product Name */}
                <div className="space-y-2 md:col-span-2">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Tên món / Sản phẩm</label>
                  <input
                    required
                    value={productName}
                    onChange={(e) => setProductName(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs"
                    placeholder="Ví dụ: Cà phê cốt dừa, Trà sữa chân trâu..."
                    type="text"
                  />
                </div>

                {/* Category Selection */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Danh mục thực đơn</label>
                  <select
                    value={categoryId}
                    onChange={(e) => setCategoryId(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all cursor-pointer outline-none text-xs"
                  >
                    {categories.map(cat => (
                      <option key={cat.id} value={cat.id}>{cat.tenDanhMuc}</option>
                    ))}
                  </select>
                </div>

                {/* Unit selection */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Đơn vị tính</label>
                  <select
                    value={unitId}
                    onChange={(e) => setUnitId(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all cursor-pointer outline-none text-xs"
                  >
                    {units.map(u => (
                      <option key={u.id} value={u.id}>{u.tenDonVi}</option>
                    ))}
                  </select>
                </div>

                {/* Cost Price */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Giá nhập kho (VND)</label>
                  <input
                    value={costPrice}
                    onChange={(e) => setCostPrice(parseInt(e.target.value) || 0)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs font-mono"
                    placeholder="Ví dụ: 15,000"
                    type="number"
                  />
                </div>

                {/* Selling Price */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Giá bán thực đơn (VND)</label>
                  <input
                    value={sellingPrice}
                    onChange={(e) => setSellingPrice(parseInt(e.target.value) || 0)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs font-mono"
                    placeholder="Ví dụ: 29,000"
                    type="number"
                  />
                </div>

                {/* Initial Stock */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Tồn kho ban đầu</label>
                  <input
                    value={initialStock}
                    onChange={(e) => setInitialStock(parseInt(e.target.value) || 0)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs"
                    type="number"
                  />
                </div>

                {/* Warning Stock limit */}
                <div className="space-y-2 md:col-span-1">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ngưỡng cảnh báo hết hàng</label>
                  <input
                    value={warningStock}
                    onChange={(e) => setWarningStock(parseInt(e.target.value) || 0)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs"
                    type="number"
                  />
                </div>

                {/* Ghi chú sản phẩm */}
                <div className="space-y-2 md:col-span-3">
                  <label className="text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ghi chú sản phẩm</label>
                  <textarea
                    value={ghiChu}
                    onChange={(e) => setGhiChu(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-lg py-2.5 px-4 focus:ring-1 focus:ring-primary focus:outline-none text-white transition-all outline-none text-xs h-20 resize-none"
                    placeholder="Nhập ghi chú thêm cho món ăn / thiết bị / sản phẩm..."
                  />
                </div>
              </div>

              {/* Modal Actions */}
              <div className="p-6 bg-white/2 border-t border-border-glass flex justify-end gap-4">
                <button
                  type="button"
                  onClick={closeModal}
                  className="px-6 py-2.5 rounded-lg text-sm font-semibold text-on-surface hover:bg-white/5 transition-all cursor-pointer"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  className="bg-primary text-on-primary px-8 py-2.5 rounded-lg text-sm font-semibold glow-button font-bold cursor-pointer"
                >
                  {isEditMode ? 'Cập nhật' : 'Lưu Sản phẩm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Child Modal to create New Group */}
      {isNewGroupModalOpen && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm">
          {/* Small Backdrop */}
          <div className="absolute inset-0" onClick={() => setIsNewGroupModalOpen(false)}></div>

          <div className="relative glass-card w-full max-w-sm rounded-2xl shadow-2xl overflow-hidden z-[120] animate-in fade-in zoom-in-95 duration-200 p-6 border border-border-glass">
            <h4 className="text-sm font-bold text-primary mb-2 flex items-center gap-1.5">
              <span className="material-symbols-outlined text-base">folder_open</span>
              Tạo nhóm sản phẩm mới
            </h4>
            <p className="text-[10px] text-on-surface-variant mb-4">
              Nhập tên nhóm mới để phân loại sản phẩm của bạn.
            </p>

            <div className="space-y-3">
              <input
                type="text"
                value={newGroupName}
                onChange={(e) => setNewGroupName(e.target.value)}
                className="w-full bg-surface-low border border-border-glass rounded-lg py-2 px-3 text-xs focus:ring-1 focus:ring-primary focus:outline-none text-white outline-none"
                placeholder="Ví dụ: Cà phê, Trà sữa, Bánh..."
                autoFocus
                onKeyDown={async (e) => {
                  if (e.key === 'Enter') {
                    await handleCreateNewGroup();
                  }
                }}
              />

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setIsNewGroupModalOpen(false);
                    setNewGroupName('');
                  }}
                  className="px-4 py-1.5 rounded-lg text-xs font-semibold text-on-surface hover:bg-white/5 transition-all cursor-pointer"
                >
                  Hủy bỏ
                </button>
                <button
                  type="button"
                  disabled={isCreatingGroup || !newGroupName.trim()}
                  onClick={handleCreateNewGroup}
                  className="bg-primary text-on-primary px-5 py-1.5 rounded-lg text-xs font-semibold glow-button cursor-pointer disabled:opacity-50"
                >
                  {isCreatingGroup ? 'Đang tạo...' : 'Tạo mới'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {productToDelete !== null && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm">
          {/* Backdrop */}
          <div className="absolute inset-0" onClick={() => { if (!isDeleting) { setProductToDelete(null); setDeleteError(null); } }}></div>

          <div className="relative glass-card w-full max-w-md rounded-2xl shadow-2xl overflow-hidden z-[120] animate-in fade-in zoom-in-95 duration-200 p-6 border border-border-glass">
            <div className="flex items-start gap-4">
              <div className="flex-shrink-0 bg-error/15 text-error p-3 rounded-full flex items-center justify-center">
                <span className="material-symbols-outlined text-2xl">warning</span>
              </div>
              <div className="flex-1 space-y-2">
                <h4 className="text-base font-bold text-white">
                  Xác nhận ẩn sản phẩm
                </h4>
                <p className="text-xs text-on-surface-variant leading-relaxed">
                  Bạn có chắc chắn muốn ẩn sản phẩm <strong className="text-white">{productToDelete.tenSanPham}</strong> này không?
                  Sản phẩm sẽ bị chuyển sang mục <span className="font-semibold text-primary">"Hàng hóa bị ẩn"</span> và không hiển thị trong menu bán hàng.
                </p>

                {deleteError && (
                  <p className="text-[11px] font-semibold text-error bg-error/10 border border-error/20 p-2 rounded-lg mt-2">
                    ⚠️ {deleteError}
                  </p>
                )}

                <div className="flex justify-end gap-3 pt-4">
                  <button
                    type="button"
                    disabled={isDeleting}
                    onClick={() => {
                      setProductToDelete(null);
                      setDeleteError(null);
                    }}
                    className="px-4 py-2 rounded-lg text-xs font-semibold text-on-surface hover:bg-white/5 transition-all cursor-pointer disabled:opacity-50"
                  >
                    Hủy bỏ
                  </button>
                  <button
                    type="button"
                    disabled={isDeleting}
                    onClick={confirmDeleteProduct}
                    className="bg-error text-white px-5 py-2 rounded-lg text-xs font-semibold hover:bg-error/80 transition-all cursor-pointer disabled:opacity-50 flex items-center gap-1.5"
                  >
                    {isDeleting ? (
                      <>
                        <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
                        <span>Đang xử lý...</span>
                      </>
                    ) : (
                      <>
                        <span className="material-symbols-outlined text-sm">delete</span>
                        <span>Ẩn sản phẩm</span>
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function InventoryPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center text-white text-sm animate-pulse">Đang tải dữ liệu thực đơn...</div>}>
      <InventoryContent />
    </Suspense>
  );
}
