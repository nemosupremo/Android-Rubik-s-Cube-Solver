/*
 * cubex.cpp
 * Cubex .505 by Eric Dietz (c) 2003 (26 Dec 03, 21 Jan 05) % 0
 * Cube Puzzle and Universal Solver.
 * Notes: readme.txt  Email: root@wrongway.org
 * NOTE: This program is unaffiliated with the Rubik's Cube Trademark.
 * This program MAY NOT be reproduced or modified outside the licensing terms
 * set forth in the readme.
 */

#include <cstdio>
#include <string>
using namespace std;
#include "cubex.h"

// definition of cube class
// Cubex constructor & destructor & count
Cubex::Cubex()
{
  shorten = true;
  cenfix = 0;
  erval = 0;
  cubeinit = false;
  solution = "";
  ResetCube();
  numcubes++;
}
Cubex::~Cubex()
{
  numcubes--;
}
int Cubex::numcubes = 0;
// version & author of the solver
const char* Cubex::ver = ".505";
const char* Cubex::author = "Eric Dietz (root@wrongway.org)";
// test the (in)eqaulity of two cubes
const bool Cubex::operator==(const Cubex &q)
{
  if (!(q.cubeinit) || !(this->cubeinit)) return false;
  bool n = true;
  for (int i = 1; i <= N; i++) {
    for (int j = 1; j <= N; j++) {
      if
      (q.Cub[i][N+1][j] != this->Cub[i][N+1][j] ||
       q.Cub[i][j][0]   != this->Cub[i][j][0]   ||
       q.Cub[0][i][j]   != this->Cub[0][i][j]   ||
       q.Cub[i][j][N+1] != this->Cub[i][j][N+1] ||
       q.Cub[N+1][i][j] != this->Cub[N+1][i][j] ||
       q.Cub[i][0][j]   != this->Cub[i][0][j]  )
        n = false;
    }
  }
  if (q.cenfix && this->cenfix) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        if
        (q.Cub[i][N][j] != this->Cub[i][N][j] ||
         q.Cub[i][j][1] != this->Cub[i][j][1] ||
         q.Cub[1][i][j] != this->Cub[1][i][j] ||
         q.Cub[i][j][N] != this->Cub[i][j][N] ||
         q.Cub[N][i][j] != this->Cub[N][i][j] ||
         q.Cub[i][1][j] != this->Cub[i][1][j])
          n = false;
      }
    }
  }
  return n;
}
const bool Cubex::operator!=(const Cubex &a) { return !(operator==(a)); }
// point a pretty cube value to an ugly array value
int *Cubex::face(int x, int y, int z)
{
  if (x+2 < 0 || x+2 > N+1 || y+2 < 0 || y+2 > N+1 || z+2 < 0 || z+2 > N+1)
    return NULL;
  return &Cub[x+2][y+2][z+2];
}
// show cube on the screen (e.g., for debugging)
const void Cubex::RenderScreen()
{
/*
  for (int i = 1; i <= N; i++) {
    for (int j = 1; j <= N; j++) printf("  ");
    printf(" ");
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[j][N+1][N+1-i]);
    printf("\n");
  }
  for (int i = 1; i <= N; i++) {
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[0][N+1-i][N+1-j]);
    printf(" ");
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[j][N+1-i][0]);
    printf(" ");
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[N+1][N+1-i][j]);
    printf(" ");
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[N+1-j][N+1-i][N+1]);
    printf("\n");
  }
  for (int i = 1; i <= N; i++) {
    for (int j = 1; j <= N; j++) printf("  ");
    printf(" ");
    for (int j = 1; j <= N; j++)
      printf(" %i", Cub[j][0][i]);
    printf("\n");
  }
*/
  printf(
"\
        %i %i %i     %i      %i\n\
        %i %i %i   %i %i %i %i\n\
        %i %i %i     %i\n\
 %i %i %i  %i %i %i  %i %i %i  %i %i %i\n\
 %i %i %i  %i %i %i  %i %i %i  %i %i %i\n\
 %i %i %i  %i %i %i  %i %i %i  %i %i %i\n\
        %i %i %i\n\
        %i %i %i\n\
        %i %i %i\n\
",
Cub[-1+2][ 2+2][ 1+2], Cub[ 0+2][ 2+2][ 1+2], Cub[ 1+2][ 2+2][ 1+2],
Cub[ 0+2][ 1+2][ 0+2], cenfix,
Cub[-1+2][ 2+2][ 0+2], Cub[ 0+2][ 2+2][ 0+2], Cub[ 1+2][ 2+2][ 0+2],
Cub[-1+2][ 0+2][ 0+2], Cub[ 0+2][ 0+2][-1+2], Cub[ 1+2][ 0+2][ 0+2], Cub[ 0+2][ 0+2][ 1+2],
Cub[-1+2][ 2+2][-1+2], Cub[ 0+2][ 2+2][-1+2], Cub[ 1+2][ 2+2][-1+2],
Cub[ 0+2][-1+2][ 0+2],
Cub[-2+2][ 1+2][ 1+2], Cub[-2+2][ 1+2][ 0+2], Cub[-2+2][ 1+2][-1+2],
Cub[-1+2][ 1+2][-2+2], Cub[ 0+2][ 1+2][-2+2], Cub[ 1+2][ 1+2][-2+2],
Cub[ 2+2][ 1+2][-1+2], Cub[ 2+2][ 1+2][ 0+2], Cub[ 2+2][ 1+2][ 1+2],
Cub[ 1+2][ 1+2][ 2+2], Cub[ 0+2][ 1+2][ 2+2], Cub[-1+2][ 1+2][ 2+2],
Cub[-2+2][ 0+2][ 1+2], Cub[-2+2][ 0+2][ 0+2], Cub[-2+2][ 0+2][-1+2],
Cub[-1+2][ 0+2][-2+2], Cub[ 0+2][ 0+2][-2+2], Cub[ 1+2][ 0+2][-2+2],
Cub[ 2+2][ 0+2][-1+2], Cub[ 2+2][ 0+2][ 0+2], Cub[ 2+2][ 0+2][ 1+2],
Cub[ 1+2][ 0+2][ 2+2], Cub[ 0+2][ 0+2][ 2+2], Cub[-1+2][ 0+2][ 2+2],
Cub[-2+2][-1+2][ 1+2], Cub[-2+2][-1+2][ 0+2], Cub[-2+2][-1+2][-1+2],
Cub[-1+2][-1+2][-2+2], Cub[ 0+2][-1+2][-2+2], Cub[ 1+2][-1+2][-2+2],
Cub[ 2+2][-1+2][-1+2], Cub[ 2+2][-1+2][ 0+2], Cub[ 2+2][-1+2][ 1+2],
Cub[ 1+2][-1+2][ 2+2], Cub[ 0+2][-1+2][ 2+2], Cub[-1+2][-1+2][ 2+2],
Cub[-1+2][-2+2][-1+2], Cub[ 0+2][-2+2][-1+2], Cub[ 1+2][-2+2][-1+2],
Cub[-1+2][-2+2][ 0+2], Cub[ 0+2][-2+2][ 0+2], Cub[ 1+2][-2+2][ 0+2],
Cub[-1+2][-2+2][ 1+2], Cub[ 0+2][-2+2][ 1+2], Cub[ 1+2][-2+2][ 1+2]
  );
}
// return true if cube is solved
const bool Cubex::IsSolved()
{
  if (!cubeinit) return false;
  int c[7], d; bool n = true;
  c[1] = Cub[2][N+1][2]; c[2] = Cub[2][2][0];
  c[3] = Cub[0][2][2];   c[4] = Cub[2][2][N+1];
  c[5] = Cub[N+1][2][2]; c[6] = Cub[2][0][2];
  for (int i = 1; i <= 6 && n == true; i++) {
    d = 0;
    for (int j = 1; j <= 6 && n == true; j++) {
      if (c[i] == j) d++;
    }
    if (d != 1)
      n = false;
  }
  for (int i = 1; i <= N && n == true; i++) {
    for (int j = 1; j <= N && n == true; j++) {
      if
      (Cub[i][N+1][j] != c[1] ||
       Cub[i][j][0]   != c[2] ||
       Cub[0][i][j]   != c[3] ||
       Cub[i][j][N+1] != c[4] ||
       Cub[N+1][i][j] != c[5] ||
       Cub[i][0][j]   != c[6])
        n = false;
    }
  }
  if (cenfix) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        if
        (Cub[i][N][j] != 0 ||
         Cub[i][j][1] != 0 ||
         Cub[1][i][j] != 0 ||
         Cub[i][j][N] != 0 ||
         Cub[N][i][j] != 0 ||
         Cub[i][1][j] != 0)
          n = false;
      }
    }
  }
  return n;
}
// create default (solved) cube model and put it in Cub[]
const void Cubex::ResetCube()
{
  solution = "";
  for (int i = 0; i <= MOV; i++) mov[i] = 0;
  for (int i = 0; i <= N+1; i++) {
    for (int j = 0; j <= N+1; j++) {
      for (int k = 0; k <= N+1; k++) {
        if (!(i-2 == 0 && j-2 == 0 && k-2 == 0))
          Cub[i][j][k] = 0;
      }
    }
  }
  for (int i = 1; i <= N; i++) {
    for (int j = 1; j <= N; j++) {
      Cub[i][N+1][j] = 1;
      Cub[i][j][0]   = 2;
      Cub[0][i][j]   = 3;
      Cub[i][j][N+1] = 4;
      Cub[N+1][i][j] = 5;
      Cub[i][0][j]   = 6;
    }
  }
  cubeinit = true;
  erval = 0;
}
// this is the series of cube rotation functions
// copy cube model so we can change it and remember the original
const void Cubex::Ctemp()
{
  for (int i = 0; i <= N+1; i++) {
    for (int j = 0; j <= N+1; j++) {
      for (int k = 0; k <= N+1; k++) {
        Tmp[i][j][k] = Cub[i][j][k];
      }
    }
  }
}
// rotate given slice left
const bool Cubex::XML(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[i][0][j] = Tmp[N+1-j][0][i];
    else if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[i][N+1][j] = Tmp[N+1-j][N+1][i];
    Cub[i][a][0]   = Tmp[N+1][a][i];
    Cub[i][a][N+1] = Tmp[0][a][i];
    Cub[0][a][i]   = Tmp[N+1-i][a][0];
    Cub[N+1][a][i] = Tmp[N+1-i][a][N+1];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][1][j] = Tmp[N+1-j][1][i];
        if (n) {
          Cub[i][1][j]--;
          if (Cub[i][1][j] < 0) Cub[i][1][j] += 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][N][j] = Tmp[N+1-j][N][i];
        if (n) {
          Cub[i][N][j]++;
          if (Cub[i][N][j] > 3) Cub[i][N][j] -= 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[i][a][1] = Tmp[N][a][i];
      Cub[i][a][N] = Tmp[1][a][i];
      Cub[1][a][i] = Tmp[N+1-i][a][1];
      Cub[N][a][i] = Tmp[N+1-i][a][N];
    }
  }
  return true;
}
// rotate given slice right
const bool Cubex::XMR(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[i][0][j] = Tmp[j][0][N+1-i];
    else if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[i][N+1][j] = Tmp[j][N+1][N+1-i];
    Cub[i][a][0]   = Tmp[0][a][N+1-i];
    Cub[i][a][N+1] = Tmp[N+1][a][N+1-i];
    Cub[0][a][i]   = Tmp[i][a][N+1];
    Cub[N+1][a][i] = Tmp[i][a][0];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][1][j] = Tmp[j][1][N+1-i];
        if (n) {
          Cub[i][1][j]++;
          if (Cub[i][1][j] > 3) Cub[i][1][j] -= 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][N][j] = Tmp[j][N][N+1-i];
        if (n) {
          Cub[i][N][j]--;
          if (Cub[i][N][j] < 0) Cub[i][N][j] += 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[i][a][1] = Tmp[1][a][N+1-i];
      Cub[i][a][N] = Tmp[N][a][N+1-i];
      Cub[1][a][i] = Tmp[i][a][N];
      Cub[N][a][i] = Tmp[i][a][1];
    }
  }
  return true;
}
// rotate given slice up
const bool Cubex::XMU(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[0][i][j] = Tmp[0][j][N+1-i];
    if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[N+1][i][j] = Tmp[N+1][j][N+1-i];
    Cub[a][i][0]   = Tmp[a][0][N+1-i];
    Cub[a][i][N+1] = Tmp[a][N+1][N+1-i];
    Cub[a][0][i]   = Tmp[a][i][N+1];
    Cub[a][N+1][i] = Tmp[a][i][0];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[1][i][j] = Tmp[1][j][N+1-i];
        if (n) {
          Cub[1][i][j]--;
          if (Cub[1][i][j] < 0) Cub[1][i][j] += 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[N][i][j] = Tmp[N][j][N+1-i];
        if (n) {
          Cub[N][i][j]++;
          if (Cub[N][i][j] > 3) Cub[N][i][j] -= 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[a][i][1] = Tmp[a][1][N+1-i];
      Cub[a][i][N] = Tmp[a][N][N+1-i];
      Cub[a][1][i] = Tmp[a][i][N];
      Cub[a][N][i] = Tmp[a][i][1];
    }
  }
  return true;
}
// rotate given slice down
const bool Cubex::XMD(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[0][i][j] = Tmp[0][N+1-j][i];
    if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[N+1][i][j] = Tmp[N+1][N+1-j][i];
    Cub[a][i][0]   = Tmp[a][N+1][i];
    Cub[a][i][N+1] = Tmp[a][0][i];
    Cub[a][0][i]   = Tmp[a][N+1-i][0];
    Cub[a][N+1][i] = Tmp[a][N+1-i][N+1];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[1][i][j] = Tmp[1][N+1-j][i];
        if (n) {
          Cub[1][i][j]++;
          if (Cub[1][i][j] > 3) Cub[1][i][j] -= 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[N][i][j] = Tmp[N][N+1-j][i];
        if (n) {
          Cub[N][i][j]--;
          if (Cub[N][i][j] < 0) Cub[N][i][j] += 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[a][i][1] = Tmp[a][N][i];
      Cub[a][i][N] = Tmp[a][1][i];
      Cub[a][1][i] = Tmp[a][N+1-i][1];
      Cub[a][N][i] = Tmp[a][N+1-i][N];
    }
  }
  return true;
}
// rotate given slice clockwise
const bool Cubex::XMC(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[i][j][0] = Tmp[N+1-j][i][0];
    if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[i][j][N+1] = Tmp[N+1-j][i][N+1];
    Cub[i][0][a]   = Tmp[N+1][i][a];
    Cub[i][N+1][a] = Tmp[0][i][a];
    Cub[0][i][a]   = Tmp[N+1-i][0][a];
    Cub[N+1][i][a] = Tmp[N+1-i][N+1][a];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][j][1] = Tmp[N+1-j][i][1];
        if (n) {
          Cub[i][j][1]++;
          if (Cub[i][j][1] > 3) Cub[i][j][1] -= 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][j][N] = Tmp[N+1-j][i][N];
        if (n) {
          Cub[i][j][N]--;
          if (Cub[i][j][N] < 0) Cub[i][j][N] += 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[i][1][a] = Tmp[N][i][a];
      Cub[i][N][a] = Tmp[1][i][a];
      Cub[1][i][a] = Tmp[N+1-i][1][a];
      Cub[N][i][a] = Tmp[N+1-i][N][a];
    }
  }
  return true;
}
// rotate given slice anticlockwise (looking from the front)
const bool Cubex::XMA(int a, bool n = true)
{
  if (a < 1 || a > N) return false;
  Ctemp();
  for (int i = 1; i <= N; i++) {
    if (a == 1)
      for (int j = 1; j <= N; j++)
        Cub[i][j][0] = Tmp[j][N+1-i][0];
    if (a == N)
      for (int j = 1; j <= N; j++)
        Cub[i][j][N+1] = Tmp[j][N+1-i][N+1];
    Cub[i][0][a]   = Tmp[0][N+1-i][a];
    Cub[i][N+1][a] = Tmp[N+1][N+1-i][a];
    Cub[0][i][a]   = Tmp[i][N+1][a];
    Cub[N+1][i][a] = Tmp[i][0][a];
  }
  if (a == 1) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][j][1] = Tmp[j][N+1-i][1];
        if (n) {
          Cub[i][j][1]--;
          if (Cub[i][j][1] < 0) Cub[i][j][1] += 4;
        }
      }
    }
  }
  else if (a == N) {
    for (int i = 2; i <= N-1; i++) {
      for (int j = 2; j <= N-1; j++) {
        Cub[i][j][N] = Tmp[j][N+1-i][N];
        if (n) {
          Cub[i][j][N]++;
          if (Cub[i][j][N] > 3) Cub[i][j][N] -= 4;
        }
      }
    }
  }
  else {
    for (int i = 2; i <= N-1; i++) {
      Cub[i][1][a] = Tmp[1][N+1-i][a];
      Cub[i][N][a] = Tmp[N][N+1-i][a];
      Cub[1][i][a] = Tmp[i][N][a];
      Cub[N][i][a] = Tmp[i][1][a];
    }
  }
  return true;
}
// the remaining rotation functions are aliases to the above rotators (with nicer names)
const void Cubex::UL() { XML(N); } // rotate top left
const void Cubex::UR() { XMR(N); } // rotate top right
const void Cubex::DL() { XML(1); } // rotate bottom left
const void Cubex::DR() { XMR(1); } // rotate bottom right
const void Cubex::LU() { XMU(1); } // rotate left up
const void Cubex::LD() { XMD(1); } // rotate left down
const void Cubex::RU() { XMU(N); } // rotate right up
const void Cubex::RD() { XMD(N); } // rotate right down
const void Cubex::FC() { XMC(1); } // rotate front clockwise
const void Cubex::FA() { XMA(1); } // rotate front anticlockwise
const void Cubex::BC() { XMC(N); } // rotate back clockwise (looking from front)
const void Cubex::BA() { XMA(N); } // rotate back anticlockwise (looking from front)
const void Cubex::ML() { XML(2); } // rotate middle left
const void Cubex::MR() { XMR(2); } // rotate middle right
const void Cubex::MU() { XMU(2); } // rotate middle up
const void Cubex::MD() { XMD(2); } // rotate middle down
const void Cubex::MC() { XMC(2); } // rotate middle clockwise
const void Cubex::MA() { XMA(2); } // rotate middle anticlockwise
const void Cubex::CL() { for (int i = 1; i <= N; i++) XML(i); } // rotate whole cube left
const void Cubex::CR() { for (int i = 1; i <= N; i++) XMR(i); } // rotate whole cube right
const void Cubex::CU() { for (int i = 1; i <= N; i++) XMU(i); } // rotate whole cube up
const void Cubex::CD() { for (int i = 1; i <= N; i++) XMD(i); } // rotate whole cube down
const void Cubex::CC() { for (int i = 1; i <= N; i++) XMC(i); } // rotate whole cube clockwise
const void Cubex::CA() { for (int i = 1; i <= N; i++) XMA(i); } // rotate whole cube anticlockwise
const void Cubex::XCL() { for (int i = 1; i <= N; i++) XML(i, false); } // rotate whole cube left w/o altering centers
const void Cubex::XCR() { for (int i = 1; i <= N; i++) XMR(i, false); } // rotate whole cube right w/o altering centers
const void Cubex::XCU() { for (int i = 1; i <= N; i++) XMU(i, false); } // rotate whole cube up w/o altering centers
const void Cubex::XCD() { for (int i = 1; i <= N; i++) XMD(i, false); } // rotate whole cube down w/o altering centers
const void Cubex::XCC() { for (int i = 1; i <= N; i++) XMC(i, false); } // rotate whole cube clockwise w/o altering centers
const void Cubex::XCA() { for (int i = 1; i <= N; i++) XMA(i, false); } // rotate whole cube anticlockwise w/o altering centers
// scramble up the cube model
const void Cubex::ScrambleCube()
{
  // come up with a better calculation for a good number of random moves based on cube size later
  int a = (N-2)*25+10;
  int n, o; string s = "";
  a += rand() % a;
  ResetCube();
  for (int i = 1; i <= a; i++) {
    n = rand() % 6 + 1; // which dimension & direction
    o = rand() % N + 1; // which layer
    switch (n) {
      case 1: XML(o); break;
      case 2: XMR(o); break;
      case 3: XMU(o); break;
      case 4: XMD(o); break;
      case 5: XMC(o); break;
      case 6: XMA(o); break;
    }
  }
  cubeinit = true;
}
// execute solution
const void Cubex::DoSolution()
{
  if (!cubeinit) return;
  string a = "";
  for (int i = 1; i <= mov[0]; i++) {
    a = solution.substr(i * 3 - 3, 3);
    if      (a == "UL.") UL();
    else if (a == "UR.") UR();
    else if (a == "DL.") DL();
    else if (a == "DR.") DR();
    else if (a == "LU.") LU();
    else if (a == "LD.") LD();
    else if (a == "RU.") RU();
    else if (a == "RD.") RD();
    else if (a == "FC.") FC();
    else if (a == "FA.") FA();
    else if (a == "BC.") BC();
    else if (a == "BA.") BA();
    else if (a == "ML.") ML();
    else if (a == "MR.") MR();
    else if (a == "MU.") MU();
    else if (a == "MD.") MD();
    else if (a == "MC.") MC();
    else if (a == "MA.") MA();
    else if (a == "CL.") CL();
    else if (a == "CR.") CR();
    else if (a == "CU.") CU();
    else if (a == "CD.") CD();
    else if (a == "CC.") CC();
    else if (a == "CA.") CA();
  }
}
// return adjacent axis on which given center is found
const int Cubex::FindCent(int a)
{
  int f = 0, x, y, z; fx = 0; fy = 0; fz = 0;
  for (int i = -1; i <= 1; i = i + 2) {
    x = Cub[i*2+2][0+2][0+2];
    y = Cub[0+2][i*2+2][0+2];
    z = Cub[0+2][0+2][i*2+2];
    if      (x == a) {
      f = i * 1; fx = i * 2; return f;
    }
    else if (y == a) {
      f = i * 2; fy = i * 2; return f;
    }
    else if (z == a) {
      f = i * 3; fz = i * 2; return f;
    }
  }
  return f;
}
// return adjacent axis on which given edge is found
const int Cubex::FindEdge(int a, int b)
{
  int f = 0, x, y, z; fx = 0; fy = 0; fz = 0;
  for (int i = -1; i <= 1; i = i + 2) {
    for (int j = -1; j <= 1; j = j + 2) {
      x = Cub[i*2+2][j+2][0+2];
      y = Cub[i+2][j*2+2][0+2];
      if      (x == a && y == b) {
        f = i * 1; fx = i * 2; fy = j; return f;
      }
      else if (y == a && x == b) {
        f = j * 2; fx = i; fy = j * 2; return f;
      }
      x = Cub[i*2+2][0+2][j+2];
      z = Cub[i+2][0+2][j*2+2];
      if      (x == a && z == b) {
        f = i * 1; fx = i * 2; fz = j; return f;
      }
      else if (z == a && x == b) {
        f = j * 3; fx = i; fz = j * 2; return f;
      }
      y = Cub[0+2][i*2+2][j+2];
      z = Cub[0+2][i+2][j*2+2];
      if      (y == a && z == b) {
        f = i * 2; fy = i * 2; fz = j; return f;
      }
      else if (z == a && y == b) {
        f = j * 3; fy = i; fz = j * 2; return f;
      }
    }
  }
  return f;
}
// return adjacent axis on which given corner is found
const int Cubex::FindCorn(int a, int b, int c)
{
  int f = 0, x, y, z; fx = 0; fy = 0; fz = 0;
  for (int i = -1; i <= 1; i = i + 2) {
    for (int j = -1; j <= 1; j = j + 2) {
      for (int k = -1; k <= 1; k = k + 2) {
        x = Cub[i*2+2][j+2][k+2];
        y = Cub[i+2][j*2+2][k+2];
        z = Cub[i+2][j+2][k*2+2];
        if      (x == a && (y == b || y == c) && (z == b || z == c)) {
          f = i * 1; fx = i * 2; fy = j; fz = k; return f;
        }
        else if (y == a && (x == b || x == c) && (z == b || z == c)) {
          f = j * 2; fx = i; fy = j * 2; fz = k; return f;
        }
        else if (z == a && (x == b || x == c) && (y == b || y == c)) {
          f = k * 3; fx = i; fy = j; fz = k * 2; return f;
        }
      }
    }
  }
  return f;
}
// routine for condensing redundant redundancies (a joke)
// this is undoubtedly the most complex routine in this class
const string Cubex::Concise(string a)
{
  // initialize stuff
  string s = a; string t = "";
  string s1 = ""; string s2 = ""; string s3 = "";
  string t1 = ""; string t2 = ""; string t3 = "";
  string zz = ""; string yy = ""; string xx = "";
  string ww = ""; string vv = ""; string uu = "";
  string mm = ""; string ll = ""; string kk = "";
  string jj = ""; string ii = ""; string hh = "";
  int b, c, g, h[2], mvs[MOV+1];
  if (mov[0] == -1) {
    mvs[0] = 0;
    for (int i = 1; i <= MOV; i++) {
      mvs[i] = 0;
      for (int j = 1; j <= i; j++) mvs[i] += mov[j];
    }
  }
  // part 1: remove middle, and whole cube moves by interpolating them
  // part 1a - getting rid of middle slice moves
  for (int i = 1; i <= s.length() / 3; i++) {
    s1 = s.substr(i * 3 - 3, 1);
    s2 = s.substr(i * 3 - 2, 1);
    if (s1 == "M") {
      if      (s2 == "U") { t += "CU.LD.RD."; }
      else if (s2 == "D") { t += "CD.LU.RU."; }
      else if (s2 == "L") { t += "CL.UR.DR."; }
      else if (s2 == "R") { t += "CR.UL.DL."; }
      else if (s2 == "C") { t += "CC.FA.BA."; }
      else if (s2 == "A") { t += "CA.FC.BC."; }
    }
    else {
      t += s1; t += s2; t += ".";
    }
  }
  s = t;
  // part 1b - interpolating whole cube moves
  c = 1;
  while (c <= s.length() / 3) {
    s1 = s.substr(c * 3 - 3, 1);
    s2 = s.substr(c * 3 - 2, 1);
    if (s1 == "C") {
      zz = "U"; yy = "D"; xx = "L"; ww = "R"; vv = "F"; uu = "B";
      mm = "L"; ll = "R"; kk = "U"; jj = "D"; ii = "C"; hh = "A";
      if      (s2 == "U") {
        zz = "F"; yy = "B"; vv = "D"; uu = "U";
        mm = "C"; ll = "A"; ii = "R"; hh = "L";
      }
      else if (s2 == "D") {
        zz = "B"; yy = "F"; vv = "U"; uu = "D";
        mm = "A"; ll = "C"; ii = "L"; hh = "R";
      }
      else if (s2 == "L") {
        xx = "F"; ww = "B"; vv = "R"; uu = "L";
        kk = "A"; jj = "C"; ii = "U"; hh = "D";
      }
      else if (s2 == "R") {
        xx = "B"; ww = "F"; vv = "L"; uu = "R";
        kk = "C"; jj = "A"; ii = "D"; hh = "U";
      }
      else if (s2 == "C") {
        xx = "D"; ww = "U"; zz = "L"; yy = "R";
        kk = "L"; jj = "R"; mm = "D"; ll = "U";
      }
      else if (s2 == "A") {
        xx = "U"; ww = "D"; zz = "R"; yy = "L";
        kk = "R"; jj = "L"; mm = "U"; ll = "D";
      }
      t = "";
      for (int i = c + 1; i <= s.length() / 3; i++) {
        t1 = s.substr(i * 3 - 3, 1);
        t2 = s.substr(i * 3 - 2, 1);
        if      (t1 == "U") { t += zz; }
        else if (t1 == "D") { t += yy; }
        else if (t1 == "L") { t += xx; }
        else if (t1 == "R") { t += ww; }
        else if (t1 == "F") { t += vv; }
        else if (t1 == "B") { t += uu; }
        else if (t1 == "C") { t += "C"; }
        if      (t2 == "L") { t += mm; }
        else if (t2 == "R") { t += ll; }
        else if (t2 == "U") { t += kk; }
        else if (t2 == "D") { t += jj; }
        else if (t2 == "C") { t += ii; }
        else if (t2 == "A") { t += hh; }
        t += ".";
      }
      c--;
      s = s.substr(0, c * 3); s += t;
    }
    c++;
  }
  // parts 2-4 are nested in this while, so that it will keep stripping out
  // moves until it goes through an entire cycle without stripping anything.
  g = 1;
  while (g > 0) {
    g = 0;
    // part 2: unshuffle possible opposite face groups, e.g., "UL.DR.UR.DL." to "UL.UR.DR.DL."
    // this will make it much easier to identify redundancies like "top left, top right" later on
    b = 0;
    while (b <= s.length() / 3 - 1 && s.length() / 3 > 0) {
      s1 = s.substr(b * 3, 2);
      t1 = s1.substr(0, 1);
      if      (t1 == "U") { t3 = "D"; }
      else if (t1 == "D") { t3 = "U"; }
      else if (t1 == "L") { t3 = "R"; }
      else if (t1 == "R") { t3 = "L"; }
      else if (t1 == "F") { t3 = "B"; }
      else if (t1 == "B") { t3 = "F"; }
      c = 0;
      s2 = s.substr(b * 3 + 3, 2);
      t2 = s2.substr(0, 1);
      while ((t2 == t1 || t2 == t3) && c <= s.length() / 3 - b - 2 && s != "") {
        if (t2 == t1 && c > 0) {
          t = s.substr(0, b * 3 + 3);
          t += s.substr(b * 3 + c * 3 + 3, 3);
          t += s.substr(b * 3 + 3, c * 3);
          t += s.substr(b * 3 + c * 3 + 6, s.length() - (b * 3 + c * 3 + 6));
          s = t;
          c = s.length() / 3;
        }
        else if (t2 == t3) {
          c++;
        }
        else {
          c = s.length() / 3;
        }
        if (c < s.length() / 3) {
          s2 = s.substr(b * 3 + c * 3 + 3, 2);
          t2 = s2.substr(0, 1);
        }
      }
      b++;
    }
    // part 3: change things like "top left, top left, top left" to simply "top right"
    b = 0;
    while (b <= s.length() / 3 - 2 && s.length() / 3 >= 3) {
      s1 = s.substr(b * 3, 2);
      s2 = s.substr(b * 3 + 3, 2);
      s3 = s.substr(b * 3 + 6, 2);
      t1 = s1.substr(0, 1);
      t2 = s1.substr(1, 1);
      if (s1 == s2 && s2 == s3) {
        if      (t2 == "L") { t3 = "R"; }
        else if (t2 == "R") { t3 = "L"; }
        else if (t2 == "U") { t3 = "D"; }
        else if (t2 == "D") { t3 = "U"; }
        else if (t2 == "C") { t3 = "A"; }
        else if (t2 == "A") { t3 = "C"; }
        g = 1;
        t = s.substr(0, b * 3);
        t += t1 + t3 + ".";
        t += s.substr(b * 3 + 9, s.length() - (b * 3 + 9));
        // change the mov[] array if necessary
        if (mov[0] == -1) {
          h[0] = 0; h[1] = 0;
          for (int i = 1; i <= MOV; i++) {
            for (int k = 0; k <= 1; k++) {
              if ((b+k+2) <= mvs[i] && (b+k+2) > mvs[i-1] && h[k] == 0) {
                mov[i]--; h[k] = 1;
                for (int j = i; j <= MOV; j++) mvs[j]--;
              }
            }
          }
        }
        //
        s = t;
        b = b - 3; if (b < -1) b = -1;
      }
      b++;
    }
    // part 4: remove explicit redundancies like "top left, top right"
    b = 0;
    while (b <= s.length() / 3 - 2 && s.length() / 3 >= 2) {
      t1 = s.substr(b * 3, 1);
      t2 = s.substr(b * 3 + 3, 1);
      s1 = s.substr(b * 3 + 1, 1);
      s2 = s.substr(b * 3 + 4, 1);
      if ((t1 == t2) &&
       ((s1 == "L" && s2 == "R") ||
       (s1 == "R" && s2 == "L") ||
       (s1 == "U" && s2 == "D") ||
       (s1 == "D" && s2 == "U") ||
       (s1 == "C" && s2 == "A") ||
       (s1 == "A" && s2 == "C"))) {
        g = 1;
        t = s.substr(0, b * 3);
        t += s.substr(b * 3 + 6, s.length() - (b * 3 + 6));
        // change the mov[] array if necessary
        if (mov[0] == -1) {
          h[0] = 0; h[1] = 0;
          for (int i = 1; i <= MOV; i++) {
            for (int k = 0; k <= 1; k++) {
              if ((b+k+1) <= mvs[i] && (b+k+1) > mvs[i-1] && h[k] == 0) {
                mov[i]--; h[k] = 1;
                for (int j = i; j <= MOV; j++) mvs[j]--;
              }
            }
          }
        }
        //
        s = t;
        b = b - 2; if (b < -1) b = -1;
      }
      b++;
    }
    // ok now it will run again if necessary, and then return the new concise string.
  }
  return s;
}
// complicated string analysis to shorten solution...
const string Cubex::Efficient(string a)
{
  string s = a;
// to be continued some day..........
  return s;
}
// Top Edges (step 1)
const string Cubex::TopEdges()
{
  string s = "";
  if (!cubeinit) return s;
  string a = ""; int b = 0, e, f, f1, m = 0;
  while (!(FindEdge(1, 2) == 2 && FindEdge(2, 1) == -3 &&
   FindEdge(1, 3) == 2 && FindEdge(3, 1) == -1 &&
   FindEdge(1, 4) == 2 && FindEdge(4, 1) == 3 &&
   FindEdge(1, 5) == 2 && FindEdge(5, 1) == 1)) {
    for (int i = 2; i <= b; i++) CR();
    if (b > 0) { s += "CR."; CR(); }
    b++; if (b > 4) b = 1;
    switch (b) {
      case 1: e = 2; break;
      case 2: e = 3; break;
      case 3: e = 4; break;
      case 4: e = 5; break;
    }
    f = FindEdge(1, e); f1 = FindEdge(e, 1);
    switch (f) {
      case 2:
        switch (f1) {
          case 3:
            s += "BC.BC.DL.DL.FC.FC.";
            BC(); BC(); DL(); DL(); FC(); FC(); break;
          case -1:
            s += "LD.LD.DR.FC.FC.";
            LD(); LD(); DR(); FC(); FC(); break;
          case 1:
            s += "RD.RD.DL.FC.FC.";
            RD(); RD(); DL(); FC(); FC(); break;
        }
        break;
      case -2:
        switch (f1) {
          case 3:
            s += "DL.DL.";
            DL(); DL(); break;
          case -1:
            s += "DR.";
            DR(); break;
          case 1:
            s += "DL.";
            DL(); break;
        }
        s += "FC.FC.";
        FC(); FC();
        break;
      case 1:
        switch (f1) {
          case -3:
            s += "FA.";
            FA(); break;
          case 3:
            s += "RU.RU.FA.RD.RD.";
            RU(); RU(); FA(); RD(); RD(); break;
          case -2:
            s += "RU.FA.RD.";
            RU(); FA(); RD(); break;
          case 2:
            s += "RD.FA.";
            RD(); FA(); break;
        }
        break;
      case -1:
        switch (f1) {
          case -3:
            s += "FC.";
            FC(); break;
          case 3:
            s += "LU.LU.FC.LD.LD.";
            LU(); LU(); FC(); LD(); LD(); break;
          case -2:
            s += "LU.FC.LD.";
            LU(); FC(); LD(); break;
          case 2:
            s += "LD.FC.";
            LD(); FC(); break;
        }
        break;
      case 3:
        switch (f1) {
          case -2:
            s += "DL.RU.FA.RD.";
            DL(); RU(); FA(); RD(); break;
          case 2:
            s += "BC.BC.DL.RU.FA.RD.";
            BC(); BC(); DL(); RU(); FA(); RD(); break;
          case -1:
            s += "LU.DR.FC.FC.LD.";
            LU(); DR(); FC(); FC(); LD(); break;
          case 1:
            s += "RU.DL.FC.FC.RD.";
            RU(); DL(); FC(); FC(); RD(); break;
        }
        break;
      case -3:
        switch (f1) {
          case -2:
            s += "DR.RU.FA.RD.";
            DR(); RU(); FA(); RD(); break;
          case 2:
            s += "FC.RD.DL.FC.FC.RU.";
            FC(); RD(); DL(); FC(); FC(); RU(); break;
          case -1:
            s += "LD.DR.FC.FC.LU.";
            LD(); DR(); FC(); FC(); LU(); break;
          case 1:
            s += "RD.DL.FC.FC.RU.";
            RD(); DL(); FC(); FC(); RU(); break;
        }
        break;
    }
    switch (b) {
      case 1: a = ""; break;
      case 2: a = "CL."; CL(); break;
      case 3: a = "CL.CL."; CL(); CL(); break;
      case 4: a = "CR."; CR(); break;
    }
    m++; if (m > 255) { cubeinit = false; s = ""; return s; }
  }
  s += a;
  if (shorten) s = Concise(s);
  mov[1] = s.length() / 3;
  return s;
}
// Top Corners (step 2)
const string Cubex::TopCorners()
{
  string s = "";
  if (!cubeinit) return s;
  string a = ""; int b = 0, c, c1, f, f1, f2, m = 0;
  while (!(FindCorn(1, 2, 5) == 2 && FindCorn(2, 1, 5) == -3 &&
   FindCorn(1, 3, 2) == 2 && FindCorn(3, 1, 2) == -1 &&
   FindCorn(1, 4, 3) == 2 && FindCorn(4, 1, 3) == 3 &&
   FindCorn(1, 5, 4) == 2 && FindCorn(5, 1, 4) == 1)) {
    for (int i = 2; i <= b; i++) CR();
    if (b > 0) { s += "CR."; CR(); }
    b++; if (b > 4) b = 1;
    switch (b) {
      case 1: c = 2; c1 = 5; break;
      case 2: c = 3; c1 = 2; break;
      case 3: c = 4; c1 = 3; break;
      case 4: c = 5; c1 = 4; break;
    }
    f = FindCorn(1, c, c1); f1 = FindCorn(c, 1, c1); f2 = FindCorn(c1, 1, c);
    switch (f) {
      case 2:
        switch (f1) {
          case 3:
            s += "BA.DL.BC.DR.RD.DR.RU.";
            BA(); DL(); BC(); DR(); RD(); DR(); RU(); break;
          case -1:
            s += "LD.DR.LU.RD.DL.RU.";
            LD(); DR(); LU(); RD(); DL(); RU(); break;
          case 1:
            s += "BC.DL.BA.FC.DR.FA.";
            BC(); DL(); BA(); FC(); DR(); FA(); break;
        }
        break;
      case -2:
        switch (f1) {
          case -3:
            s += "DR.";
            DR(); break;
          case 3:
            s += "DL.";
            DL(); break;
          case -1:
            s += "DL.DL.";
            DL(); DL(); break;
        }
        s += "FC.DL.FA.DR.RD.DR.RU.";
        FC(); DL(); FA(); DR(); RD(); DR(); RU();
        break;
      case 1:
        switch (f1) {
          case -3:
            s += "RD.DL.RU.";
            RD(); DL(); RU(); break;
          case 3:
            s += "RU.DR.RD.DR.RD.DR.RU.";
            RU(); DR(); RD(); DR(); RD(); DR(); RU(); break;
          case -2:
            s += "DL.FC.DR.FA.";
            DL(); FC(); DR(); FA(); break;
          case 2:
            s += "RD.DL.RU.DR.RD.DL.RU.";
            RD(); DL(); RU(); DR(); RD(); DL(); RU(); break;
        }
        break;
      case -1:
        switch (f1) {
          case -3:
            s += "LD.DR.LU.FC.DR.FA.";
            LD(); DR(); LU(); FC(); DR(); FA(); break;
          case 3:
            s += "DL.FC.DL.FA.";
            DL(); FC(); DL(); FA(); break;
          case -2:
            s += "RD.DR.RU.";
            RD(); DR(); RU(); break;
          case 2:
            s += "LU.DL.LD.FC.DL.FA.";
            LU(); DL(); LD(); FC(); DL(); FA(); break;
        }
        break;
      case 3:
        switch (f1) {
          case -2:
            s += "DR.RD.DR.RU.";
            DR(); RD(); DR(); RU(); break;
          case 2:
            s += "BC.FC.DL.BA.FA.";
            BC(); FC(); DL(); BA(); FA(); break;
          case -1:
            s += "BA.DR.BC.RD.DR.RU.";
            BA(); DR(); BC(); RD(); DR(); RU(); break;
          case 1:
            s += "FC.DL.FA.";
            FC(); DL(); FA(); break;
        }
        break;
      case -3:
        switch (f1) {
          case -2:
            s += "FC.DR.FA.";
            FC(); DR(); FA(); break;
          case 2:
            s += "FA.DL.FC.DL.FC.DL.FA.";
            FA(); DL(); FC(); DL(); FC(); DL(); FA(); break;
          case -1:
            s += "DR.RD.DL.RU.";
            DR(); RD(); DL(); RU(); break;
          case 1:
            s += "FC.DR.FA.DL.FC.DR.FA.";
            FC(); DR(); FA(); DL(); FC(); DR(); FA(); break;
        }
        break;
    }
    switch (b) {
      case 1: a = ""; break;
      case 2: a = "CL."; CL(); break;
      case 3: a = "CL.CL."; CL(); CL(); break;
      case 4: a = "CR."; CR(); break;
    }
    m++; if (m > 255) { cubeinit = false; s = ""; return s; }
  }
  s += a;
  if (shorten) s = Concise(s);
  mov[2] = s.length() / 3;
  return s;
}
// Middle Edges (step 3)
const string Cubex::MiddleEdges()
{
  string s = "";
  if (!cubeinit) return s;
  string a = ""; int b = 0, e, e1, f, f1, m = 0;
  while (!(FindEdge(2, 5) == -3 && FindEdge(5, 2) == 1 &&
   FindEdge(3, 2) == -1 && FindEdge(2, 3) == -3 &&
   FindEdge(4, 3) == 3 && FindEdge(3, 4) == -1 &&
   FindEdge(5, 4) == 1 && FindEdge(4, 5) == 3)) {
    for (int i = 2; i <= b; i++) CR();
    if (b > 0) { s += "CR."; CR(); }
    b++; if (b > 4) b = 1;
    switch (b) {
      case 1: e = 2; e1 = 5; break;
      case 2: e = 3; e1 = 2; break;
      case 3: e = 4; e1 = 3; break;
      case 4: e = 5; e1 = 4; break;
    }
    a = "";
    f = FindEdge(e, e1); f1 = FindEdge(e1, e);
    while (!(f == -2 || f1 == -2)) {
      if (f == -1 && f1 == -3) { a = "CR."; CR(); }
      if (f == -1 && f1 == 3) { a = "CL.CL."; CL(); CL(); }
      if (f == 1 && f1 == 3) { a = "CL."; CL(); }
      if (f == -3 && f1 == -1) { a = "CR."; CR(); }
      if (f == 3 && f1 == -1) { a = "CL.CL."; CL(); CL(); }
      if (f == 3 && f1 == 1) { a = "CL."; CL(); }
      s += a; s += "RD.DR.RU.DR.FC.DL.FA.";
      RD(); DR(); RU(); DR(); FC(); DL(); FA();
      if (a == "CL.") { s += "CR."; CR(); }
      if (a == "CL.CL.") { s += "CR.CR."; CR(); CR(); }
      if (a == "CR.") { s += "CL."; CL(); }
      a = "";
      f = FindEdge(e,  e1); f1 = FindEdge(e1, e);
    }
    if (f == -2) {
      switch (f1) {
        case -3:
          s += "DL.DL."; DL(); DL(); break;
        case -1:
          s += "DL."; DL(); break;
        case 1:
          s += "DR."; DR(); break;
      }
      s += "FC.DL.FA.DL.RD.DR.RU.";
      FC(); DL(); FA(); DL(); RD(); DR(); RU();
    }
    else if (f1 == -2) {
      switch (f) {
        case -3:
          s += "DL."; DL(); break;
        case 3:
          s += "DR."; DR(); break;
        case 1:
          s += "DL.DL."; DL(); DL(); break;
      }
      s += "RD.DR.RU.DR.FC.DL.FA.";
      RD(); DR(); RU(); DR(); FC(); DL(); FA();
    }
    switch (b) {
      case 1: a = ""; break;
      case 2: a = "CL."; CL(); break;
      case 3: a = "CL.CL."; CL(); CL(); break;
      case 4: a = "CR."; CR(); break;
    }
    m++; if (m > 255) { cubeinit = false; s = ""; return s; }
  }
  s += a;
  if (shorten) s = Concise(s);
  mov[3] = s.length() / 3;
  return s;
}
// Bottom Edges Orient (step 4)
const string Cubex::BottomEdgesOrient()
{
  string s = "";
  if (!cubeinit) return s;
  int eo[4], a = 0, b, m = 0, r;
  while (a != 4) {
    eo[0] = Cub[0+2][-2+2][-1+2];
    eo[1] = Cub[-1+2][-2+2][0+2];
    eo[2] = Cub[0+2][-2+2][1+2];
    eo[3] = Cub[1+2][-2+2][0+2];
    a = 0; r = 0;
    for (int i = 0; i <= 3; i++) {
      b = i + 1; if (b > 3) b = 0;
      if (eo[i] == 6) {
        a++;
        if (eo[b] == 6) r = i;
      }
    }
    if (a == 0) {
      s += "RD.BC.DL.BA.DR.RU.";
      RD(); BC(); DL(); BA(); DR(); RU();
    }
    else if (a == 2) {
      switch (r) {
        case 1:
          s += "CR."; CR(); break;
        case 2:
          s += "CL.CL."; CL(); CL(); break;
        case 3:
          s += "CL."; CL(); break;
      }
      s += "RD.DL.BC.DR.BA.RU.";
      RD(); DL(); BC(); DR(); BA(); RU();
      switch (r) {
        case 1:
          s += "CL."; CL(); break;
        case 2:
          s += "CR.CR."; CR(); CR(); break;
        case 3:
          s += "CR."; CR(); break;
      }
    }
    m++; if (m > 255) { cubeinit = false; s = ""; return s; }
  }
  if (shorten) s = Concise(s);
  mov[4] = s.length() / 3;
  return s;
}
// Bottom Edges Position (step 5)
const string Cubex::BottomEdgesPosition()
{
  string s = "";
  if (!cubeinit) return s;
  int ep[4][2], a = 0, b, l, m = 0, t = 0;
  while (a != 4) {
    ep[0][0] = Cub[0+2][0+2][-2+2]; ep[0][1] = Cub[0+2][-1+2][-2+2];
    ep[1][0] = Cub[-2+2][0+2][0+2]; ep[1][1] = Cub[-2+2][-1+2][0+2];
    ep[2][0] = Cub[0+2][0+2][2+2]; ep[2][1] = Cub[0+2][-1+2][2+2];
    ep[3][0] = Cub[2+2][0+2][0+2]; ep[3][1] = Cub[2+2][-1+2][0+2];
    a = 0; l = 0;
    for (int i = 0; i <= 3; i++) {
      b = i - 1; if (b < 0) b = 3;
      if (ep[i][0] == ep[i][1]) {
        a++;
      }
      else {
        if (ep[b][0] != ep[b][1]) l = i;
      }
    }
    if (a < 2) {
      t++; if (t > 3) t = 0;
      DL();
    }
    else {
      switch (t) {
        case 1:
          s += "DL."; break;
        case 2:
          s += "DL.DL."; break;
        case 3:
          s += "DR."; break;
      }
      t = 0;
    }
    if (a == 2) {
      switch (l) {
        case 1:
          s += "CR."; CR(); break;
        case 2:
          s += "CL.CL."; CL(); CL(); break;
        case 3:
          s += "CL."; CL(); break;
      }
      s += "RD.DL.DL.RU.DR.RD.DR.RU.";
      RD(); DL(); DL(); RU(); DR(); RD(); DR(); RU();
      switch (l) {
        case 1:
          s += "CL."; CL(); break;
        case 2:
          s += "CR.CR."; CR(); CR(); break;
        case 3:
          s += "CR."; CR(); break;
      }
    }
    m++; if (m > 255) { cubeinit = false; s = ""; return s; }
  }
  if (shorten) s = Concise(s);
  mov[5] = s.length() / 3;
  return s;
}
// Bottom Corners Position (step 6)
const string Cubex::BottomCornersPosition()
{
  string s = "";
  if (!cubeinit) return s;
  int cp[4][2], a = 0, l, m = 0;
  while (a != 4) {
    cp[0][0] = FindCorn(6, 2, 3); cp[0][1] = (fx < 0 && fy < 0 && fz < 0);
    cp[1][0] = FindCorn(6, 3, 4); cp[1][1] = (fx < 0 && fy < 0 && fz > 0);
    cp[2][0] = FindCorn(6, 4, 5); cp[2][1] = (fx > 0 && fy < 0 && fz > 0);
    cp[3][0] = FindCorn(6, 5, 2); cp[3][1] = (fx > 0 && fy < 0 && fz < 0);
    a = 0; l = 0;
    for (int i = 0; i <= 3; i++) {
      if (cp[i][1] == 1) {
        a++; l = i;
      }
    }
    if (a < 4) {
      switch (l) {
        case 1:
          s += "CR."; CR(); break;
        case 2:
          s += "CL.CL."; CL(); CL(); break;
        case 3:
          s += "CL."; CL(); break;
      }
      s += "RD.DR.LD.DL.RU.DR.LU.DL.";
      RD(); DR(); LD(); DL(); RU(); DR(); LU(); DL();
      switch (l) {
        case 1:
          s += "CL."; CL(); break;
        case 2:
          s += "CR.CR."; CR(); CR(); break;
        case 3:
          s += "CR."; CR(); break;
      }
      m++; if (m > 255) { cubeinit = false; s = ""; return s; }
    }
  }
  if (shorten) s = Concise(s);
  mov[6] = s.length() / 3;
  return s;
}
// Bottom Corners Orient (step 7)
const string Cubex::BottomCornersOrient()
{
  string s = "";
  if (!cubeinit) return s;
  int co[4], a = -1, b, b1, b2, d, m = 0, r;
  while (a != 0) {
    co[0] = FindCorn(6, 2, 5);
    co[1] = FindCorn(6, 3, 2);
    co[2] = FindCorn(6, 4, 3);
    co[3] = FindCorn(6, 5, 4);
    a = 0; r = 0; d = 0;
    for (int i = 0; i <= 3; i++) {
      if (co[i] != -2) a++;
    }
    if (a > 0) {
      for (int i = 0; i <= 3; i++) {
        b = i + 2; if (b > 3) b = b - 4;
        b1 = i - 1; if (b1 < 0) b1 = 3;
        b2 = i + 1; if (b2 > 3) b2 = 0;
        if (co[i] != -2) {
          switch (a) {
            case 2:
              if (co[b1] != -2) r = i;
              if (co[b] != -2 && (co[i] == 1 || co[i] == -1)) {
                d = 1; if (i == 0) r = 0; else r = i - 1;
              }
              break;
            case 3:
              if (co[b1] != -2 && co[b2] != -2) r = i;
              break;
            case 4:
              if (co[i] == 1 && co[b1] == 1 && i < 2) r = i;
              break;
          }
        }
      }
      switch (r) {
        case 1:
          s += "CR."; CR(); break;
        case 2:
          s += "CL.CL."; CL(); CL(); break;
        case 3:
          s += "CL."; CL(); break;
      }
      if (a == 4) {
        s += "RD.DL.DL.RU.DR.RD.DR.RU.";
        s += "LD.DR.DR.LU.DL.LD.";
        s += "DR.LU.DL.LD.DL.LU.";
        s += "RD.DL.DL.RU.DR.RD.DR.RU.";
        RD(); DL(); DL(); RU(); DR(); RD(); DR(); RU();
        LD(); DR(); DR(); LU(); DL(); LD();
        DR(); LU(); DL(); LD(); DL(); LU();
        RD(); DL(); DL(); RU(); DR(); RD(); DR(); RU();
      }
      else if (a == 3 && Cub[2+2][-1+2][-1+2] == 6) {
        s += "FC.DR.DR.FA.DL.FC.DL.FA.";
        s += "BC.DL.DL.BA.DR.BC.DR.BA.";
        FC(); DR(); DR(); FA(); DL(); FC(); DL(); FA();
        BC(); DL(); DL(); BA(); DR(); BC(); DR(); BA();
      }
      else if (a == 2 && d == 0 && Cub[1+2][-1+2][-2+2] == 6) {
        s += "LD.DR.LU.DR.LD.DL.DL.LU.";
        s += "RD.DL.RU.DL.RD.DR.DR.RU.";
        LD(); DR(); LU(); DR(); LD(); DL(); DL(); LU();
        RD(); DL(); RU(); DL(); RD(); DR(); DR(); RU();
      }
      else {
        s += "RD.DL.DL.RU.DR.RD.DR.RU.";
        s += "LD.DR.DR.LU.DL.LD.DL.LU.";
        RD(); DL(); DL(); RU(); DR(); RD(); DR(); RU();
        LD(); DR(); DR(); LU(); DL(); LD(); DL(); LU();
      }
      switch (r) {
        case 1:
          s += "CL."; CL(); break;
        case 2:
          s += "CR.CR."; CR(); CR(); break;
        case 3:
          s += "CR."; CR(); break;
      }
      m++; if (m > 255) { cubeinit = false; s = ""; return s; }
    }
  }
  if (shorten) s = Concise(s);
  mov[7] = s.length() / 3;
  return s;
}
// Centers Rotation (for bitmapped/picture cubes) (step 8)
const string Cubex::CentersRotate()
{
  string s = "";
  if (!cubeinit) return s;
  mov[8] = 0;
  if (!cenfix) return s;
  int a = 0, b, c, d, p;
  for (int q = 1; q <= 6; q++) {
    a += Cub[0+2][1+2][0+2];
    if (q % 2 == 0) XCU(); else XCA();
  }
  if (a % 2 != 0) { cubeinit = false; s = ""; return s; }
  for (int q = 1; q <= 6; q++) {
    b = Cub[0+2][1+2][0+2];
    switch (b) {
      case 2:
        // top = 2
        s += "UL.RU.LD.UR.UR.RD.LU.";
        s += "UL.RU.LD.UR.UR.RD.LU.";
        UL(); RU(); LD(); UR(); UR(); RD(); LU();
        UL(); RU(); LD(); UR(); UR(); RD(); LU();
        break;
      case 1:
        d = 0;
        for (p = 1; p <= 4; p++) {
          if (d == 0) {
            c = Cub[-1+2][0+2][0+2];
            if (c == 3) {
              // top = 1, left = 3
              s += "MD.MR.MU.UL.MD.ML.MU.UR.";
              MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
              d = 1;
            }
          }
          s += "CL."; XCL();
        }
        if (d == 0) {
          for (p = 1; p <= 4; p++) {
            if (d == 0) {
              c = Cub[-1+2][0+2][0+2];
              if (c == 1) {
                // top = 1, left = 1
                s += "CC.";
                s += "UL.RU.LD.UR.UR.RD.LU.";
                s += "UL.RU.LD.UR.UR.RD.LU.";
                s += "CA.";
                s += "MD.MR.MU.UL.MD.ML.MU.UR.";
                CC();
                UL(); RU(); LD(); UR(); UR(); RD(); LU();
                UL(); RU(); LD(); UR(); UR(); RD(); LU();
                CA();
                MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
                d = 1;
              }
            }
            s += "CL."; XCL();
          }
        }
        if (d == 0) {
          c = Cub[0+2][-1+2][0+2];
          switch (c) {
            case 3:
              // top = 1, bottom = 3
              s += "CC.";
              s += "MD.MR.MU.UL.MD.ML.MU.UR.";
              s += "CA.";
              s += "MD.MR.MU.UL.MD.ML.MU.UR.";
              CC();
              MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
              CA();
              MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
              break;
            case 1:
              // top = 1, bottom = 1
              s += "CC.CC.";
              s += "UL.RU.LD.UR.UR.RD.LU.";
              s += "UL.RU.LD.UR.UR.RD.LU.";
              s += "CA.";
              s += "MD.MR.MU.UL.MD.ML.MU.UR.";
              s += "CA.";
              s += "MD.MR.MU.UL.MD.ML.MU.UR.";
              CC(); CC();
              UL(); RU(); LD(); UR(); UR(); RD(); LU();
              UL(); RU(); LD(); UR(); UR(); RD(); LU();
              CA();
              MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
              CA();
              MD(); MR(); MU(); UL(); MD(); ML(); MU(); UR();
              break;
          }
        }
        break;
      case 3:
        d = 0;
        for (p = 1; p <= 4; p++) {
          if (d == 0) {
            c = Cub[1+2][0+2][0+2];
            if (c == 1) {
              // top = 3, right = 1
              s += "MD.ML.MU.UR.MD.MR.MU.UL.";
              MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
              d = 1;
            }
          }
          s += "CL."; XCL();
        }
        if (d == 0) {
          for (p = 1; p <= 4; p++) {
            if (d == 0) {
              c = Cub[1+2][0+2][0+2];
              if (c == 3) {
                // top = 3, right = 3
                s += "CA.";
                s += "UL.RU.LD.UR.UR.RD.LU.";
                s += "UL.RU.LD.UR.UR.RD.LU.";
                s += "CC.";
                s += "MD.ML.MU.UR.MD.MR.MU.UL.";
                CA();
                UL(); RU(); LD(); UR(); UR(); RD(); LU();
                UL(); RU(); LD(); UR(); UR(); RD(); LU();
                CC();
                MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
                d = 1;
              }
            }
            s += "CL."; XCL();
          }
        }
        if (d == 0) {
          c = Cub[0+2][-1+2][0+2];
          switch (c) {
            case 1:
              // top = 3, bottom = 1
              s += "CA.";
              s += "MD.ML.MU.UR.MD.MR.MU.UL.";
              s += "CC.";
              s += "MD.ML.MU.UR.MD.MR.MU.UL.";
              CA();
              MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
              CC();
              MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
              break;
            case 3:
              // top = 3, bottom = 3
              s += "CA.CA.";
              s += "UL.RU.LD.UR.UR.RD.LU.";
              s += "UL.RU.LD.UR.UR.RD.LU.";
              s += "CC.";
              s += "MD.ML.MU.UR.MD.MR.MU.UL.";
              s += "CC.";
              s += "MD.ML.MU.UR.MD.MR.MU.UL.";
              CA(); CA();
              UL(); RU(); LD(); UR(); UR(); RD(); LU();
              UL(); RU(); LD(); UR(); UR(); RD(); LU();
              CC();
              MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
              CC();
              MD(); ML(); MU(); UR(); MD(); MR(); MU(); UL();
              break;
          }
        }
        break;
    }    
    if (q % 2 == 0) {
      s += "CU."; XCU();
    }
    else {
      s += "CA."; XCA();
    }
  }
  if (shorten) s = Concise(s);
  mov[8] = s.length() / 3;
  return s;
}
// solve the cube (uses many other routines also)
// slightly complicated...
const int Cubex::SolveCube()
{
  // make sure cube was initialized
  if (!cubeinit) return 1;
  // set up buffers and counters and such...
  int Rub[5][5][5], Fac[7][2], mvs[MOV+1], m = -1, n;
  string s = ""; string t = ""; string p = "";
  // make sure that the cube has the proper cubelets...
  cubeinit = false;
  // check that all the centers are present
  for (int i = 1; i <= 6; i++) {
    if (FindCent(i) == 0) {
      erval = 1; return erval;
    }
  }
  // buffer the cube so we can interpolate it to a specific color arrangement to check for edges and corners...
  for (int i = -2; i <= 2; i++) {
    for (int j = -2; j <= 2; j++) {
      for (int k = -2; k <= 2; k++) {
        Rub[i+2][j+2][k+2] = Cub[i+2][j+2][k+2];
      }
    }
  }
  // interpolate the cube...
  Fac[0][0] = 0;
  Fac[1][0] = Cub[0+2][2+2][0+2]; Fac[2][0] = Cub[0+2][0+2][-2+2];
  Fac[3][0] = Cub[-2+2][0+2][0+2]; Fac[4][0] = Cub[0+2][0+2][2+2];
  Fac[5][0] = Cub[2+2][0+2][0+2]; Fac[6][0] = Cub[0+2][-2+2][0+2];
  for (int i = 0; i <= 6; i++) {
    Fac[Fac[i][0]][1] = i;
  }
  // apply interpolation
  for (int i = -1; i <= 1; i++) {
    for (int j = -1; j <= 1; j++) {
      Cub[i+2][2+2][j+2] = Fac[Cub[i+2][2+2][j+2]][1];
      Cub[i+2][j+2][-2+2] = Fac[Cub[i+2][j+2][-2+2]][1];
      Cub[-2+2][i+2][j+2] = Fac[Cub[-2+2][i+2][j+2]][1];
      Cub[i+2][j+2][2+2] = Fac[Cub[i+2][j+2][2+2]][1];
      Cub[2+2][i+2][j+2] = Fac[Cub[2+2][i+2][j+2]][1];
      Cub[i+2][-2+2][j+2] = Fac[Cub[i+2][-2+2][j+2]][1];
    }
  }
  // check that all edges and corners are present
  for (int i = 1; i <= 4; i++) {
    int j = 1;
    if (i < 4) j = i + 1;
    if (FindEdge(1, i + 1) == 0 ||
     FindEdge(6, i + 1) == 0 ||
     FindEdge(i + 1, j + 1) == 0 ||
     FindCorn(1, i + 1, j + 1) == 0 ||
     FindCorn(6, i + 1, j + 1) == 0) {
      m = 0;
    }
  }
  // return cube to pre-interpolated status
  for (int i = -2; i <= 2; i++) {
    for (int j = -2; j <= 2; j++) {
      for (int k = -2; k <= 2; k++) {
        Cub[i+2][j+2][k+2] = Rub[i+2][j+2][k+2];
      }
    }
  }
  // if any flags went off during checking then return error 1 (improper cubelets)
  if (m == 0) { erval = 1; return erval; }
  cubeinit = true;
  // cube seems to have ok cubelets, so try to solve it...
  for (int i = 1; i <= MOV; i++) mvs[i] = 0;
  // try to solve the cube from each possible starting orientation (to find the fastest solution)...
  for (int q = 1; q <= 24; q++) {
    // buffer old cube
    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        for (int k = -2; k <= 2; k++) {
          Rub[i+2][j+2][k+2] = Cub[i+2][j+2][k+2];
        }
      }
    }
    // interpolate so that centers are in order...
    Fac[0][0] = 0;
    Fac[1][0] = Cub[0+2][2+2][0+2]; Fac[2][0] = Cub[0+2][0+2][-2+2];
    Fac[3][0] = Cub[-2+2][0+2][0+2]; Fac[4][0] = Cub[0+2][0+2][2+2];
    Fac[5][0] = Cub[2+2][0+2][0+2]; Fac[6][0] = Cub[0+2][-2+2][0+2];
    for (int i = 0; i <= 6; i++) {
      Fac[Fac[i][0]][1] = i;
    }
    // apply interpolation
    for (int i = -1; i <= 1; i++) {
      for (int j = -1; j <= 1; j++) {
        Cub[i+2][2+2][j+2] = Fac[Cub[i+2][2+2][j+2]][1];
        Cub[i+2][j+2][-2+2] = Fac[Cub[i+2][j+2][-2+2]][1];
        Cub[-2+2][i+2][j+2] = Fac[Cub[-2+2][i+2][j+2]][1];
        Cub[i+2][j+2][2+2] = Fac[Cub[i+2][j+2][2+2]][1];
        Cub[2+2][i+2][j+2] = Fac[Cub[2+2][i+2][j+2]][1];
        Cub[i+2][-2+2][j+2] = Fac[Cub[i+2][-2+2][j+2]][1];
      }
    }
    // if we dont care about centers, we can imply their orientation liberally
    if (!cenfix) {
      Cub[0+2][1+2][0+2] = 0;
      Cub[0+2][0+2][-1+2] = 0;
      Cub[-1+2][0+2][0+2] = 0;
      Cub[0+2][0+2][1+2] = 0;
      Cub[1+2][0+2][0+2] = 0;
      Cub[0+2][-1+2][0+2] = 0;
    }
    // solve the cube...
    t = TopEdges();
    t += TopCorners();
    t += MiddleEdges();
    if (!cubeinit && erval == 0) { erval = 4; }
    t += BottomEdgesOrient();
    if (!cubeinit && erval == 0) { erval = 5; }
    t += BottomEdgesPosition();
    if (!cubeinit && erval == 0) { erval = 2; }
    t += BottomCornersPosition();
    if (!cubeinit && erval == 0) { erval = 6; }
    t += BottomCornersOrient();
    if (!cubeinit && erval == 0) { erval = 7; }
    t += CentersRotate();
    if (!cubeinit && erval == 0) { erval = 3; }
    // errors above:
    // 2-nondescript parity, 3-center orientation, 4-backward centers or corners,
    // 5-edge flip parity, 6-edge swap parity, 7-corner rotation parity
    if (shorten) {
      mov[0] = -1; t = Concise(p + t); mov[0] = 0;
    }
    t = Efficient(t);
    n = t.length() / 3;
    // if this was shortest solution found so far, run with it...
    if (n < m || m < 0) {
      m = n; s = t;
      for (int i = 1; i <= MOV; i++) {
        mvs[i] = mov[i];
      }
      // if we dont care about centers, apply the implied orientations
      if (!cenfix) {
        Rub[0+2][1+2][0+2] = (4 - Cub[0+2][1+2][0+2]) % 4;
        Rub[0+2][0+2][-1+2] = (4 - Cub[0+2][0+2][-1+2]) % 4;
        Rub[-1+2][0+2][0+2] = (4 - Cub[-1+2][0+2][0+2]) % 4;
        Rub[0+2][0+2][1+2] = (4 - Cub[0+2][0+2][1+2]) % 4;
        Rub[1+2][0+2][0+2] = (4 - Cub[1+2][0+2][0+2]) % 4;
        Rub[0+2][-1+2][0+2] = (4 - Cub[0+2][-1+2][0+2]) % 4;
      }
    }
    // restore old (pre-interpolated) cube
    for (int i = -2; i <= 2; i++) {
      for (int j = -2; j <= 2; j++) {
        for (int k = -2; k <= 2; k++) {
          Cub[i+2][j+2][k+2] = Rub[i+2][j+2][k+2];
        }
      }
    }
    // rotate to next orientation and try again to see if we get a shorter solution
    if (q % 4 == 0) {
      p += "CU."; XCU();
    }
    else {
      p += "CL."; XCL();
    }
    if (q == 12) {
      p = "CU.CU.CR."; XCU(); XCU(); XCR();
    }
    else if (q == 24) {
      XCD(); XCD(); XCR();
    }
  }
  // set mov array...
  for (int i = 1; i <= MOV; i++) {
    mov[i] = mvs[i];
  }
  // return error if one was found
  if (!cubeinit) return erval;
  mov[0] = m;
  // set solution and return...
  solution = s;
  return 0;
}
// end of cube class definitions
