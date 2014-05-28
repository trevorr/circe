/*******************************************************************************
 * Copyright 2014 Trevor Robinson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
#include "crc32c_sse42.hpp"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _MSC_VER
# include <windows.h>
static uint64_t get_ticks() { LARGE_INTEGER t; QueryPerformanceCounter(&t); return t.QuadPart; }
static uint64_t get_tick_freq() { LARGE_INTEGER t; QueryPerformanceFrequency(&t); return t.QuadPart; }
#else
# include <time.h>
static uint64_t get_ticks() {
    timespec t;
    clock_gettime(CLOCK_PROCESS_CPUTIME_ID, &t);
    return (uint64_t) t.tv_sec * 1000000000 + t.tv_nsec;
}
static uint64_t get_tick_freq() { return 1000000000; }
#endif

const size_t MB = 1024*1024;

int main(int argc, char* argv[]) {
    const bool full = argc > 1 && strcmp(argv[1], "--full") == 0;

    crc32c_initialize();

    const uint64_t freq = get_tick_freq();

    const size_t minlen = full ? 1 : MB;
    const size_t maxlen = 128*MB;
    const int reps = full ? 2 : 1;

    const char* ctrlfname = "crc32c.gpi";
    const char* datafname = "crc32c.data";
    const char* outputfname = "crc32c.png";
    FILE* ctrlfile = fopen(ctrlfname, "w");
    fprintf(ctrlfile, "set term png size 1600, 1200\n");
    fprintf(ctrlfile, "set output '%s'\n", outputfname);
    fprintf(ctrlfile, "set logscale x\n");
    fprintf(ctrlfile, "set xlabel 'bytes'\n");
    fprintf(ctrlfile, "set ylabel 'MB/s'\n");
    fprintf(ctrlfile, "set key right bottom\n");
    fprintf(ctrlfile, "plot '%s'", datafname);
    FILE* datafile = fopen(datafname, "w");
    int plotindex = 0;

    char* pc = (char*) malloc(maxlen);
    memset(pc, 0, maxlen);

    {
        uint64_t* pq = (uint64_t*) pc;
        uint64_t best = 0;
        for (int rep = 0; rep < reps; ++rep) {
            uint64_t start = get_ticks();
            for (size_t i = 0; i < maxlen / 8; ++i)
                pq[i] = i;
            uint64_t stop = get_ticks();
            uint64_t ticks = stop - start;
            if (rep == 0 || ticks < best)
                best = ticks;
        }
        float rate = (float) maxlen / MB / ((float) best / (float) freq);
        printf("64-bit fill rate = %.2f MB/s\n", rate);
        fprintf(ctrlfile, " index %d with lines title '64-bit fill'", plotindex++);
        fprintf(datafile, "1 %f\n" SIZE_T_FORMAT " %f\n\n\n", rate, maxlen, rate);
    }

    {
        uint64_t best = 0;
        for (int rep = 0; rep < reps; ++rep) {
            uint64_t start = get_ticks();
            for (size_t i = 0; i < maxlen; ++i)
                pc[i] = (char) i;
            uint64_t stop = get_ticks();
            uint64_t ticks = stop - start;
            if (rep == 0 || ticks < best)
                best = ticks;
        }
        float rate = (float) maxlen / MB / ((float) best / (float) freq);
        printf("8-bit fill rate = %.2f MB/s\n", rate);
        fprintf(ctrlfile, ", '' index %d with lines title '8-bit fill'", plotindex++);
        fprintf(datafile, "1 %f\n" SIZE_T_FORMAT " %f\n\n\n", rate, maxlen, rate);
    }

    const int max_chunk_sizes = 2;
    const int full_configs[][max_chunk_sizes] = {
        { 0 },
        { 64, 0 }, { 128, 0 }, { 256, 0 }, { 512, 0 }, { 1024, 0 }, { 2048, 0 }, { 4096, 0 },
        { 64, 4096 }, { 128, 4096 }, { 256, 4096 }, { 512, 4096 }, { 1024, 4096 } };
    const int fast_configs[][max_chunk_sizes] = { { 0 }, { 64, 4096 } };
    const int (*configs)[max_chunk_sizes] = full ? full_configs : fast_configs;
    const size_t configs_size = full ? sizeof(full_configs) : sizeof(fast_configs);
    const int config_count = (int) (configs_size / sizeof(configs[0]));
    for (int i = 0; i < config_count; ++i) {
        printf("chunk config %d:\n", i);
        const chunk_config* config = 0;
        for (int j = 0; j < max_chunk_sizes && configs[i][j]; ++j) {
            printf("  chunk size %d: %d words\n", j, configs[i][j]);
            config = new chunk_config(configs[i][j], config);
        }

        fprintf(ctrlfile, ", '' index %d with lines title '%d/%d words'",
            plotindex++, configs[i][0], configs[i][1]);

        const float m = full ? 1.1f : 2.0f;
        for (size_t len = minlen; len <= maxlen; len = (int) ceilf((float) len * m)) {
            if (len > 256)
                len = (len + 7) & ~7;

            printf("[%d/%d] length " SIZE_T_FORMAT ": ", i, config_count, len);

            uint32_t crc;
            size_t ofs;
            uint64_t best = 0;
            for (int rep = 0; rep < reps; ++rep) {
                crc = 0;
                ofs = 0;
                uint64_t start = get_ticks();
                for (; ofs + len <= maxlen; ofs += len) {
                    crc = crc32c(crc, pc + ofs, len, config);
                }
                uint64_t stop = get_ticks();
                uint64_t ticks = stop - start;
                if (rep == 0 || ticks < best)
                    best = ticks;
            }

            float rate = (float) ofs / MB / ((float) best / (float) freq);
            printf("crc = 0x%08x, rate = %.2f MB/s\n", crc, rate);
            fprintf(datafile, SIZE_T_FORMAT " %f\n", len, rate);
        }
        fprintf(datafile, "\n\n");

        while (config) {
            const chunk_config* next = config->next;
            delete config;
            config = next;
        }
    }
    fprintf(ctrlfile, "\n");

    free(pc);
    fclose(datafile);
    fclose(ctrlfile);
    return 0;
}
