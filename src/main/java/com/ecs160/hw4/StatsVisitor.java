package com.ecs160.hw4;

abstract class StatsVisitor {
    abstract void visit(Post post);
    abstract double getResult();
}
