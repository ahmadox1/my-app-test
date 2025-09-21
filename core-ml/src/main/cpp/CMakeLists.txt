cmake_minimum_required(VERSION 3.22.1)
project(llama_bridge)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    llama_bridge
    SHARED
    llama_bridge.cpp
)

find_library(
    log-lib
    log
)

find_library(
    jnigraphics-lib
    jnigraphics
)

target_link_libraries(
    llama_bridge
    ${log-lib}
    ${jnigraphics-lib}
)
