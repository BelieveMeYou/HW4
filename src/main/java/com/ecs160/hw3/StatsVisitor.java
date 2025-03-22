package com.ecs160.hw1;

abstract class StatsVisitor {
    abstract void visit(Post post);
    abstract double getResult();
}
