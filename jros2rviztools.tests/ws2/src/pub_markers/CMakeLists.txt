cmake_minimum_required(VERSION 3.5)
project(pub_markers)

if(NOT CMAKE_CXX_STANDARD)
  set(CMAKE_CXX_STANDARD 14)
endif()

if(CMAKE_COMPILER_IS_GNUCXX OR CMAKE_CXX_COMPILER_ID MATCHES "Clang")
  add_compile_options(-Wall -Wextra -Wpedantic)
endif()

find_package(ament_cmake REQUIRED)
find_package(rclcpp REQUIRED)
find_package(visualization_msgs REQUIRED)

add_executable(node src/node.cpp)
ament_target_dependencies(node rclcpp visualization_msgs)

install(TARGETS
  node
  DESTINATION lib/${PROJECT_NAME})

ament_package()
