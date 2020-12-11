! Generated by GenChem.py - DO NOT EDIT


!+ Defines indices and M,O2,H2O :

integer, parameter :: M   =71
integer, parameter :: O2  =72
integer, parameter :: H2O =73

integer, parameter :: &
  OP          =   1  &
  , OD          =   2  &
  , OH          =   3  &
  , HO2         =   4  &
  , CH3O2       =   5  &
  , C2H5O2      =   6  &
  , secC4H9O2   =   7  &
  , IsopO2      =   8  &
  , CH2O2CH2OH  =   9  &
  , CH3CHO2CH2OH=  10

integer, parameter :: &
  oXylOHO2    =  11  &
  , CH3COCHO2CH3=  12  &
  , MemalO2     =  13  &
  , MVKO2       =  14  &
  , MTO2        =  15  &
  , O3          =  16  &
  , NO          =  17  &
  , NO2         =  18  &
  , PAN         =  19  &
  , NO3         =  20

integer, parameter :: &
  N2O5        =  21  &
  , HNO3        =  22  &
  , HONO        =  23  &
  , CH3COO2     =  24  &
  , MVKetone    =  25  &
  , HCOCHO      =  26  &
  , CH3COCHO    =  27  &
  , Memaldial   =  28  &
  , CH3COC2H5   =  29  &
  , MTKetone    =  30

integer, parameter :: &
  ProdApinO3  =  31  &
  , ProdApinNO3 =  32  &
  , ProdApinOH  =  33  &
  , ProdLimoO3  =  34  &
  , ProdLimoNO3 =  35  &
  , ProdLimoOH  =  36  &
  , HCHO        =  37  &
  , CH3CHO      =  38  &
  , C2H6        =  39  &
  , nC4H10      =  40

integer, parameter :: &
  C2H4        =  41  &
  , C3H6        =  42  &
  , oXylene     =  43  &
  , Isoprene    =  44  &
  , Apinene     =  45  &
  , Limonene    =  46  &
  , CH3O2H      =  47  &
  , C2H5OOH     =  48  &
  , BURO2H      =  49  &
  , ETRO2H      =  50

integer, parameter :: &
  PRRO2H      =  51  &
  , OXYO2H      =  52  &
  , MEKO2H      =  53  &
  , MemalO2H    =  54  &
  , MVKO2H      =  55  &
  , ISRO2H      =  56  &
  , MTO2H       =  57  &
  , H2O2        =  58  &
  , CH3COO2H    =  59  &
  , CH3OH       =  60

integer, parameter :: &
  C2H5OH      =  61  &
  , CH3COCH2OH  =  62  &
  , H2          =  63  &
  , CO          =  64  &
  , CH4         =  65  &
  , SO2         =  66  &
  , Sulphate    =  67  &
  , BLOC        =  68  &
  , BSOC        =  69  &
  , ALOC        =  70
  
  !+
  ! If zero timestep then we need to do nothing
  
  IF (DTP .LE. 0.) RETURN
  
  ! Set number of Gauss-Seidel iterations of the
  ! implicit Euler and the BDF second order method
  
  NrGSEul = 2
  NrGSBDF = 2
  
  ! Set absolute (mol/cm3) and relative (%/100) error
  ! tolerances
  
  DO NS = 1,NSPEC
    RTOL(NS) = 0.10
  ENDDO
  
  ATOLF = 1.0
  
  !+
  ! Assigns ATOL
  !+
  ATOL(OP          ) = 1.00e+15*ATOLF 
  ATOL(OD          ) = 1.00e+15*ATOLF 
  ATOL(OH          ) = 1.00e+15*ATOLF 
  ATOL(HO2         ) = 1.00e+15*ATOLF 
  ATOL(CH3O2       ) = 1.00e+15*ATOLF 
  ATOL(C2H5O2      ) = 1.00e+15*ATOLF 
  ATOL(secC4H9O2   ) = 1.00e+15*ATOLF 
  ATOL(IsopO2      ) = 1.00e+15*ATOLF 
  ATOL(CH2O2CH2OH  ) = 1.00e+15*ATOLF 
  ATOL(CH3CHO2CH2OH) = 1.00e+15*ATOLF 
  ATOL(oXylOHO2    ) = 1.00e+15*ATOLF 
  ATOL(CH3COCHO2CH3) = 1.00e+15*ATOLF 
  ATOL(MemalO2     ) = 1.00e+15*ATOLF 
  ATOL(MVKO2       ) = 1.00e+15*ATOLF 
  ATOL(MTO2        ) = 1.00e+15*ATOLF 
  ATOL(O3          ) = 5.00e+10*ATOLF 
  ATOL(NO          ) = 2.50e+08*ATOLF 
  ATOL(NO2         ) = 2.50e+09*ATOLF 
  ATOL(PAN         ) = 2.50e+09*ATOLF 
  ATOL(NO3         ) = 1.00e+15*ATOLF 
  ATOL(N2O5        ) = 1.00e+15*ATOLF 
  ATOL(HNO3        ) = 2.50e+08*ATOLF 
  ATOL(HONO        ) = 1.00e+15*ATOLF 
  ATOL(CH3COO2     ) = 1.00e+15*ATOLF 
  ATOL(MVKetone    ) = 1.00e+15*ATOLF 
  ATOL(HCOCHO      ) = 1.00e+15*ATOLF 
  ATOL(CH3COCHO    ) = 1.00e+15*ATOLF 
  ATOL(Memaldial   ) = 1.00e+15*ATOLF 
  ATOL(CH3COC2H5   ) = 1.00e+15*ATOLF 
  ATOL(MTKetone    ) = 1.00e+15*ATOLF 
  ATOL(ProdApinO3  ) = 2.50e+09*ATOLF 
  ATOL(ProdApinNO3 ) = 2.50e+09*ATOLF 
  ATOL(ProdApinOH  ) = 2.50e+09*ATOLF 
  ATOL(ProdLimoO3  ) = 2.50e+09*ATOLF 
  ATOL(ProdLimoNO3 ) = 2.50e+09*ATOLF 
  ATOL(ProdLimoOH  ) = 2.50e+09*ATOLF 
  ATOL(HCHO        ) = 2.50e+09*ATOLF 
  ATOL(CH3CHO      ) = 2.50e+09*ATOLF 
  ATOL(C2H6        ) = 5.00e+09*ATOLF 
  ATOL(nC4H10      ) = 5.00e+09*ATOLF 
  ATOL(C2H4        ) = 2.50e+09*ATOLF 
  ATOL(C3H6        ) = 2.50e+09*ATOLF 
  ATOL(oXylene     ) = 2.50e+09*ATOLF 
  ATOL(Isoprene    ) = 2.50e+08*ATOLF 
  ATOL(Apinene     ) = 2.50e+08*ATOLF 
  ATOL(Limonene    ) = 2.50e+08*ATOLF 
  ATOL(CH3O2H      ) = 2.50e+09*ATOLF 
  ATOL(C2H5OOH     ) = 1.00e+15*ATOLF 
  ATOL(BURO2H      ) = 1.00e+15*ATOLF 
  ATOL(ETRO2H      ) = 1.00e+15*ATOLF 
  ATOL(PRRO2H      ) = 1.00e+15*ATOLF 
  ATOL(OXYO2H      ) = 1.00e+15*ATOLF 
  ATOL(MEKO2H      ) = 1.00e+15*ATOLF 
  ATOL(MemalO2H    ) = 1.00e+15*ATOLF 
  ATOL(MVKO2H      ) = 1.00e+15*ATOLF 
  ATOL(ISRO2H      ) = 1.00e+15*ATOLF 
  ATOL(MTO2H       ) = 1.00e+15*ATOLF 
  ATOL(H2O2        ) = 2.50e+09*ATOLF 
  ATOL(CH3COO2H    ) = 1.00e+15*ATOLF 
  ATOL(CH3OH       ) = 2.50e+09*ATOLF 
  ATOL(C2H5OH      ) = 2.50e+09*ATOLF 
  ATOL(CH3COCH2OH  ) = 1.00e+15*ATOLF 
  ATOL(H2          ) = 1.00e+15*ATOLF 
  ATOL(CO          ) = 1.00e+15*ATOLF 
  ATOL(CH4         ) = 1.00e+15*ATOLF 
  ATOL(SO2         ) = 2.50e+09*ATOLF 
  ATOL(Sulphate    ) = 2.50e+09*ATOLF 
  ATOL(BLOC        ) = 2.50e+09*ATOLF 
  ATOL(BSOC        ) = 2.50e+09*ATOLF 
  ATOL(ALOC        ) = 2.50e+09*ATOLF 